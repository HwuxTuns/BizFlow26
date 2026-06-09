package com.example.promotion.service;

import com.example.promotion.dto.BundleItemDTO;
import com.example.promotion.dto.CartItemPriceRequest;
import com.example.promotion.dto.CartItemPriceResponse;
import com.example.promotion.dto.PromotionDTO;
import com.example.promotion.dto.PromotionTargetDTO;
import com.example.promotion.entity.BundleItem;
import com.example.promotion.entity.Promotion;
import com.example.promotion.entity.PromotionTarget;
import com.example.promotion.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionServiceImpl.class);

    private final PromotionRepository promotionRepository;
    private final CacheManager cacheManager;
    private final RestTemplate restTemplate;

    @Value("${nifi.promotion.signal-url:}")
    private String nifiSignalUrl;

    @Value("${services.customer-service:http://customer-service:8085}")
    private String customerServiceUrl;

    private static final Long SYSTEM_USER_ID = 0L; // System account for notifications
    private static final Long OWNER_USER_ID = 1L;  // Owner user ID

    public PromotionServiceImpl(
            PromotionRepository promotionRepository,
            CacheManager cacheManager,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.promotionRepository = promotionRepository;
        this.cacheManager = cacheManager;
        this.restTemplate = restTemplateBuilder.build();
    }

    /* ================== QUERY ================== */

    @Override
    public List<PromotionDTO> getAllPromotions() {
        List<Promotion> all = promotionRepository.findAll();
        log.info("[getAllPromotions] Total promotions in DB: {}", all.size());
        
        List<PromotionDTO> result = all.stream()
                .peek(promo -> {
                    boolean hasQuota = hasRemainingQuota(promo);
                    Integer max = normalizeMaxQuantity(promo.getMaxQuantity());
                    Integer used = normalizeUsedQuantity(promo.getUsedQuantity());
                    log.info("[getAllPromotions] Checking code={}, active={}, used={}, max={}, hasQuota={}", 
                        promo.getCode(), promo.getActive(), used, max, hasQuota);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        log.info("[getAllPromotions] Returned: {} promotions (including exhausted)", result.size());
        return result;
    }

    @Override
    public List<PromotionDTO> searchPromotions(String query, String discountType, String targetType, Long targetId, Boolean active) {
        Promotion.DiscountType parsedDiscountType = parseDiscountType(discountType);
        PromotionTarget.TargetType parsedTargetType = parseTargetType(targetType);
        String q = query != null && !query.isBlank() ? query.trim() : null;

        return promotionRepository.searchPromotions(q, parsedDiscountType, active, parsedTargetType, targetId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String generatePromotionCode(String name) {
        String prefix = buildPromoPrefixFromName(name);
        if (prefix == null || prefix.isBlank()) {
            prefix = "KM";
        }
        List<String> existing = promotionRepository.findCodesByPrefix(prefix);
        java.util.Set<String> used = existing == null
                ? java.util.Collections.emptySet()
                : existing.stream().map(code -> code.toUpperCase()).collect(Collectors.toSet());
        for (int i = 1; i < 1000; i++) {
            String suffix = String.format("%03d", i);
            String candidate = prefix + "-" + suffix;
            if (!used.contains(candidate.toUpperCase())) {
                return candidate;
            }
        }
        return prefix + "-" + String.format("%03d", (int) (Math.random() * 900 + 100));
    }

    @Override
    @Cacheable(cacheNames = "activePromotions", key = "'active'")
    public List<PromotionDTO> getActivePromotions() {
        List<Promotion> allActive = promotionRepository.findActivePromotions(LocalDateTime.now());
        log.info("[getActivePromotions] Total active promotions: {}", allActive.size());
        
        List<PromotionDTO> result = allActive.stream()
                .peek(promo -> {
                    Integer max = normalizeMaxQuantity(promo.getMaxQuantity());
                    Integer used = normalizeUsedQuantity(promo.getUsedQuantity());
                    boolean hasQuota = hasRemainingQuota(promo);
                    log.info("[getActivePromotions] Code={}, used={}, max={}, hasQuota={}", 
                        promo.getCode(), used, max, hasQuota);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        log.info("[getActivePromotions] Returned: {} promotions (including exhausted)", result.size());
        return result;
    }

    @Override
    @Cacheable(cacheNames = "promotionByCode", key = "#code", unless = "#result == null")
    public PromotionDTO getPromotionByCode(String code) {
        return promotionRepository.findByCode(code)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    @Cacheable(cacheNames = "activePromotionByCode", key = "#code", unless = "#result == null")
    public PromotionDTO getActivePromotionByCode(String code) {
        return promotionRepository.findActivePromotionByCode(code, LocalDateTime.now())
                .map(this::toDTO)
                .orElse(null);
    }

    /* ================== COMMAND ================== */

    @Override
    @CachePut(cacheNames = "promotionByCode", key = "#result.code", unless = "#result == null")
    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    public PromotionDTO createPromotion(PromotionDTO dto) {
        validatePromotionDTO(dto, null);
        Promotion promotion = new Promotion();
        promotion.setTargets(new ArrayList<>());
        promotion.setBundleItems(new ArrayList<>());

        applyDTOToEntity(dto, promotion);
        PromotionDTO created = toDTO(promotionRepository.save(promotion));
        refreshActivePromotionCache(created);
        emitNifiSignalAfterCommit("PROMOTION_CREATED", created);
        return created;
    }

    @Override
    @CachePut(cacheNames = "promotionByCode", key = "#result.code", unless = "#result == null")
    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    public PromotionDTO updatePromotion(Long id, PromotionDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("Promotion id must not be null");
        }
        validatePromotionDTO(dto, id);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        String previousCode = promotion.getCode();

        if (promotion.getTargets() == null) {
            promotion.setTargets(new ArrayList<>());
        } else {
            promotion.getTargets().clear();
        }

        if (promotion.getBundleItems() == null) {
            promotion.setBundleItems(new ArrayList<>());
        } else {
            promotion.getBundleItems().clear();
        }

        applyDTOToEntity(dto, promotion);
        PromotionDTO updated = toDTO(promotionRepository.save(promotion));
        if (updated != null && previousCode != null && !previousCode.equals(updated.getCode())) {
            evictPromotionByCode(previousCode);
            evictActivePromotionByCode(previousCode);
        }
        refreshActivePromotionCache(updated);
        emitNifiSignalAfterCommit("PROMOTION_UPDATED", updated);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    public void deletePromotion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Promotion id must not be null");
        }

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        PromotionDTO snapshot = toDTO(promotion);

        if (promotion != null) {
            promotionRepository.delete(promotion);
            evictPromotionByCode(promotion.getCode());
            evictActivePromotionByCode(promotion.getCode());
        }
        emitNifiSignalAfterCommit("PROMOTION_DELETED", snapshot);
    }

    @Override
    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    public void deactivatePromotion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Promotion id must not be null");
        }

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setActive(false);
        promotionRepository.save(promotion);
        evictPromotionByCode(promotion.getCode());
        evictActivePromotionByCode(promotion.getCode());
    }

    @Override
    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    public void activatePromotion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Promotion id must not be null");
        }

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setActive(true);
        promotionRepository.save(promotion);
        evictPromotionByCode(promotion.getCode());
        evictActivePromotionByCode(promotion.getCode());
    }

    /* ================== MAPPING ================== */

    private PromotionDTO toDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(promotion.getId());
        dto.setCode(promotion.getCode());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setDiscountType(normalizeDiscountType(promotion));
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setActive(promotion.getActive());
        dto.setMaxQuantity(promotion.getMaxQuantity());
        dto.setUsedQuantity(normalizeUsedQuantity(promotion.getUsedQuantity()));
        dto.setRemainingQuantity(calculateRemainingQuantity(promotion));
        dto.setIsExhausted(isQuotaExhausted(promotion));

        try {
            List<PromotionTarget> targets = promotion.getTargets();
            dto.setTargets(
                    targets == null
                            ? Collections.emptyList()
                            : targets.stream()
                                .map(this::toTargetDTO)
                                .collect(Collectors.toList())
            );
        } catch (RuntimeException ex) {
            dto.setTargets(Collections.emptyList());
        }

        try {
            List<BundleItem> bundleItems = promotion.getBundleItems();
            dto.setBundleItems(
                    bundleItems == null
                            ? Collections.emptyList()
                            : bundleItems.stream()
                                .map(this::toBundleItemDTO)
                                .collect(Collectors.toList())
            );
        } catch (RuntimeException ex) {
            dto.setBundleItems(Collections.emptyList());
        }

        return dto;
    }

    private PromotionTargetDTO toTargetDTO(PromotionTarget target) {
        PromotionTargetDTO dto = new PromotionTargetDTO();
        dto.setId(target.getId());
        dto.setTargetType(target.getTargetType());
        dto.setTargetId(target.getTargetId());
        return dto;
    }

    private BundleItemDTO toBundleItemDTO(BundleItem item) {
        BundleItemDTO dto = new BundleItemDTO();
        dto.setId(item.getId());
        dto.setMainProductId(item.getMainProductId());
        dto.setMainQuantity(item.getMainQuantity());
        dto.setGiftProductId(item.getGiftProductId());
        dto.setGiftQuantity(item.getGiftQuantity());
        dto.setGiftDiscountType(item.getGiftDiscountType());
        dto.setGiftDiscountValue(item.getGiftDiscountValue());
        dto.setStatus(item.getStatus());
        dto.setProductId(item.getMainProductId());
        dto.setQuantity(item.getMainQuantity());
        return dto;
    }

    private void applyDTOToEntity(PromotionDTO dto, Promotion promotion) {
        promotion.setCode(dto.getCode());
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        if (dto.getDiscountType() != null) {
            promotion.setDiscountType(dto.getDiscountType());
            promotion.setPromotionType(mapPromotionTypeFromDiscount(dto.getDiscountType()));
        }
        promotion.setDiscountValue(dto.getDiscountValue());
        
        // Ensure startDate is set and not in future
        LocalDateTime startDate = dto.getStartDate();
        if (startDate == null) {
            startDate = LocalDateTime.now();
        } else if (startDate.isAfter(LocalDateTime.now())) {
            // If startDate is in future, set to now to make promotion active immediately
            startDate = LocalDateTime.now();
        }
        promotion.setStartDate(startDate);
        
        LocalDateTime endDate = dto.getEndDate();
        if (endDate == null) {
            // Default: 30 days from now
            endDate = LocalDateTime.now().plusDays(30);
        }
        promotion.setEndDate(endDate);
        
        Boolean active = dto.getActive();
        promotion.setActive(active != null ? active : true);
        
        // 🔧 FIX: Handle maxQuantity and usedQuantity logic properly
        // Lưu giữ maxQuantity cũ trước khi đặt cái mới
        Integer oldMaxQuantity = promotion.getMaxQuantity();
        Integer newMaxQuantity = normalizeMaxQuantity(dto.getMaxQuantity());
        promotion.setMaxQuantity(newMaxQuantity);
        
        // Xử lý logic reset usedQuantity dựa trên thay đổi maxQuantity
        handleUsedQuantityOnMaxQuantityChange(promotion, oldMaxQuantity, newMaxQuantity);

        if (dto.getTargets() != null) {
            if (promotion.getTargets() == null) {
                promotion.setTargets(new ArrayList<>());
            }
            for (PromotionTargetDTO tdto : dto.getTargets()) {
                PromotionTarget target = new PromotionTarget();
                target.setTargetType(tdto.getTargetType());
                target.setTargetId(tdto.getTargetId());
                target.setPromotion(promotion);
                promotion.getTargets().add(target);
            }
        }

        if (dto.getBundleItems() != null) {
            if (promotion.getBundleItems() == null) {
                promotion.setBundleItems(new ArrayList<>());
            }
            for (BundleItemDTO bdto : dto.getBundleItems()) {
                BundleItem item = new BundleItem();
                Long mainProductId = bdto.getMainProductId() != null ? bdto.getMainProductId() : bdto.getProductId();
                Integer mainQty = bdto.getMainQuantity() != null ? bdto.getMainQuantity() : bdto.getQuantity();
                Long giftProductId = bdto.getGiftProductId() != null ? bdto.getGiftProductId() : bdto.getProductId();
                Integer giftQty = bdto.getGiftQuantity() != null ? bdto.getGiftQuantity() : bdto.getQuantity();
                String giftDiscountType = bdto.getGiftDiscountType() != null ? bdto.getGiftDiscountType() : "FREE";
                Double giftDiscountValueObj = bdto.getGiftDiscountValue();
                Double giftDiscountValue = giftDiscountValueObj != null ? giftDiscountValueObj : 0.0;
                String status = bdto.getStatus() != null ? bdto.getStatus() : "ACTIVE";

                item.setProductId(mainProductId);
                item.setQuantity(mainQty);
                item.setMainProductId(mainProductId);
                item.setMainQuantity(mainQty);
                item.setGiftProductId(giftProductId);
                item.setGiftQuantity(giftQty);
                item.setGiftDiscountType(giftDiscountType);
                item.setGiftDiscountValue(giftDiscountValue);
                item.setStatus(status);
                item.setPromotion(promotion);
                promotion.getBundleItems().add(item);
            }
        }
    }

    private void validatePromotionDTO(PromotionDTO dto, Long currentId) {
        if (dto == null) {
            throw new IllegalArgumentException("Promotion payload must not be null");
        }

        String code = dto.getCode() != null ? dto.getCode().trim() : "";
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống");
        }

        String name = dto.getName() != null ? dto.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống");
        }

        if (dto.getDiscountType() == null) {
            throw new IllegalArgumentException("Loại giảm giá phải được chọn");
        }

        if (dto.getDiscountValue() == null) {
            throw new IllegalArgumentException("Giá trị giảm phải được nhập");
        }

        Integer maxQuantity = dto.getMaxQuantity();
        if (maxQuantity != null && maxQuantity < 0) {
            throw new IllegalArgumentException("Số lượng áp dụng không được âm");
        }

        double value = dto.getDiscountValue();
        if (value < 0) {
            throw new IllegalArgumentException("Giá trị giảm không được âm");
        }
        if (dto.getDiscountType() == Promotion.DiscountType.PERCENT && (value <= 0 || value > 100)) {
            throw new IllegalArgumentException("Phần trăm giảm phải từ 1 đến 100");
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (dto.getStartDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống");
        }
        if (dto.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày kết thúc không được để trống");
        }

        validateTargets(dto);
        validateBundleItems(dto);

        promotionRepository.findByCode(code)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại. Vui lòng chọn mã khác.");
                });
    }

    private void validateTargets(PromotionDTO dto) {
        if (dto.getTargets() == null) {
            return;
        }
        for (PromotionTargetDTO target : dto.getTargets()) {
            if (target == null) {
                throw new IllegalArgumentException("Promotion target must not be null");
            }
            if (target.getTargetType() == null) {
                throw new IllegalArgumentException("Promotion target type must be provided");
            }
            if (target.getTargetId() == null || target.getTargetId() <= 0) {
                throw new IllegalArgumentException("Promotion target id must be a positive number");
            }
        }
    }

    private void validateBundleItems(PromotionDTO dto) {
        if (dto.getBundleItems() == null) {
            return;
        }
        if (dto.getDiscountType() != null && dto.getDiscountType() != Promotion.DiscountType.BUNDLE
                && !dto.getBundleItems().isEmpty()) {
            throw new IllegalArgumentException("Only bundle promotions may include bundle items");
        }
        if (dto.getDiscountType() == Promotion.DiscountType.BUNDLE && dto.getBundleItems().isEmpty()) {
            throw new IllegalArgumentException("Bundle promotions must include at least one bundle item");
        }
        for (BundleItemDTO item : dto.getBundleItems()) {
            if (item == null) {
                throw new IllegalArgumentException("Bundle item must not be null");
            }
            Long mainProductId = item.getMainProductId() != null ? item.getMainProductId() : item.getProductId();
            Long giftProductId = item.getGiftProductId() != null ? item.getGiftProductId() : item.getProductId();
            Integer mainQty = item.getMainQuantity() != null ? item.getMainQuantity() : item.getQuantity();
            Integer giftQty = item.getGiftQuantity() != null ? item.getGiftQuantity() : item.getQuantity();

            if (mainProductId == null || mainProductId <= 0) {
                throw new IllegalArgumentException("Bundle mainProductId must be a positive number");
            }
            if (giftProductId == null || giftProductId <= 0) {
                throw new IllegalArgumentException("Bundle giftProductId must be a positive number");
            }
            if (mainQty == null || mainQty <= 0) {
                throw new IllegalArgumentException("Bundle mainQuantity must be a positive number");
            }
            if (giftQty == null || giftQty <= 0) {
                throw new IllegalArgumentException("Bundle giftQuantity must be a positive number");
            }
        }
    }

    private Promotion.DiscountType normalizeDiscountType(Promotion promotion) {
        Promotion.DiscountType raw = promotion.getDiscountType();
        String promotionType = promotion.getPromotionType();

        Promotion.DiscountType fromPromotionType = mapPromotionType(promotionType);
        if (fromPromotionType != null) {
            if (raw == null || raw == Promotion.DiscountType.BUNDLE || raw == Promotion.DiscountType.FREE_GIFT
                    || raw == Promotion.DiscountType.FIXED_AMOUNT) {
                return fromPromotionType;
            }
        }

        return mapDiscountType(raw);
    }

    private Promotion.DiscountType mapDiscountType(Promotion.DiscountType raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case FIXED_AMOUNT -> Promotion.DiscountType.FIXED;
            case FREE_GIFT -> Promotion.DiscountType.BUNDLE;
            default -> raw;
        };
    }

    private Promotion.DiscountType mapPromotionType(String promotionType) {
        if (promotionType == null || promotionType.isBlank()) {
            return null;
        }
        return switch (promotionType) {
            case "PERCENT" -> Promotion.DiscountType.PERCENT;
            case "FIXED_AMOUNT", "FIXED" -> Promotion.DiscountType.FIXED;
            case "FREE_GIFT", "BUNDLE" -> Promotion.DiscountType.BUNDLE;
            default -> null;
        };
    }

    private Promotion.DiscountType parseDiscountType(String discountType) {
        if (discountType == null || discountType.isBlank()) {
            return null;
        }
        try {
            return Promotion.DiscountType.valueOf(discountType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String buildPromoPrefixFromName(String name) {
        String base = normalizePromotionName(name);
        if (base.isBlank()) {
            return "KM";
        }
        String[] words = base.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        String shortCode = initials.length() >= 2
                ? initials.toString()
                : base.replace(" ", "").substring(0, Math.min(4, base.length())).toUpperCase();
        return "KM" + shortCode;
    }

    private String normalizePromotionName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private PromotionTarget.TargetType parseTargetType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return null;
        }
        try {
            return PromotionTarget.TargetType.valueOf(targetType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String mapPromotionTypeFromDiscount(Promotion.DiscountType discountType) {
        if (discountType == null) {
            return null;
        }
        return switch (discountType) {
            case PERCENT -> "PERCENT";
            case FIXED, FIXED_AMOUNT -> "FIXED_AMOUNT";
            case FREE_GIFT -> "FREE_GIFT";
            case BUNDLE -> "BUNDLE";
        };
    }

    private void evictPromotionByCode(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache("promotionByCode");
        if (cache != null) {
            cache.evict(code);
        }
    }

    private void evictActivePromotionByCode(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache("activePromotionByCode");
        if (cache != null) {
            cache.evict(code);
        }
    }

    private void refreshActivePromotionCache(PromotionDTO dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache("activePromotionByCode");
        if (cache == null) {
            return;
        }
        String codeKey = dto.getCode();
        if (isActiveNow(dto, LocalDateTime.now())) {
            if (codeKey != null) {
                cache.put(codeKey, dto);
            }
        } else {
            if (codeKey != null) {
                cache.evict(codeKey);
            }
        }
    }

    private boolean isActiveNow(PromotionDTO dto, LocalDateTime now) {
        if (dto.getActive() != null && !dto.getActive()) {
            return false;
        }
        if (dto.getStartDate() != null && dto.getStartDate().isAfter(now)) {
            return false;
        }
        return dto.getEndDate() == null || !dto.getEndDate().isBefore(now);
    }

    private void emitNifiSignalAfterCommit(String eventType, PromotionDTO dto) {
        if (!isNifiSignalEnabled()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendNifiSignal(eventType, dto);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendNifiSignal(eventType, dto);
            }
        });
    }

    private boolean isNifiSignalEnabled() {
        return nifiSignalUrl != null && !nifiSignalUrl.isBlank();
    }

    @Override
    public List<CartItemPriceResponse> calculateCartItemPrices(CartItemPriceRequest request) {
        return calculateCartItemPricesInternal(request, false);
    }

    @Override
    public List<CartItemPriceResponse> calculateAndConsumeCartItemPrices(CartItemPriceRequest request) {
        return calculateCartItemPricesInternal(request, true);
    }

    private List<CartItemPriceResponse> calculateCartItemPricesInternal(CartItemPriceRequest request, boolean consumeQuota) {
        List<CartItemPriceResponse> responses = new ArrayList<>();
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return responses;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromos = promotionRepository.findActivePromotions(now);
        log.info("===== CALCULATE CART PRICES (consumeQuota={}) =====", consumeQuota);
        log.info("findActivePromotions returned {} promotions at {}", activePromos.size(), now);
        for (Promotion promo : activePromos) {
            log.info("  PROMO: {} | Code={} | StartDt={} | EndDt={} | UsedQty={} | MaxQty={}", 
                promo.getId(), promo.getCode(), promo.getStartDate(), promo.getEndDate(), 
                promo.getUsedQuantity(), promo.getMaxQuantity());
        }

        for (CartItemPriceRequest.CartItem item : request.getItems()) {
            log.info("Processing item: productId={} | qty={} | basePrice={}", 
                item.getProductId(), item.getQuantity(), item.getBasePrice());
            CartItemPriceResponse response = calculateSingleItemPrice(item, activePromos, consumeQuota);
            responses.add(response);
            log.info("  Result: appliedPromo={} | finalPrice={} | discount={}", 
                response.getPromoCode(), response.getFinalPrice(), response.getDiscount());
        }

        return responses;
    }

    private CartItemPriceResponse calculateSingleItemPrice(
            CartItemPriceRequest.CartItem item,
            List<Promotion> promotions,
            boolean consumeQuota
    ) {
        BigDecimal basePrice = item == null || item.getBasePrice() == null
                ? BigDecimal.ZERO
                : item.getBasePrice();
        int demandQuantity = normalizeDemandQuantity(item);

        if (item == null || item.getProductId() == null || demandQuantity <= 0) {
            return new CartItemPriceResponse(
                    item == null ? null : item.getProductId(),
                    basePrice,
                    basePrice,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    demandQuantity
            );
        }

        Promotion bestPromo = null;
        BigDecimal bestPrice = null;
        int bestConsumption = 0;

        log.info("  Evaluate {} promotions for item {}", promotions.size(), item.getProductId());
        for (Promotion promo : promotions) {
            log.info("    EVAL: promo {} (code={}) | hasRemainingQuota={}", 
                promo.getId(), promo.getCode(), hasRemainingQuota(promo));
            PromotionSelection candidate = evaluatePromotionForItem(promo, item, basePrice, demandQuantity);
            if (candidate == null) {
                log.info("      -> evaluatePromotionForItem returned null (doesn't apply to item or no quota)");
                continue;
            }
            log.info("      -> candidate price={} | consumeAmount={}", candidate.finalPrice, candidate.consumeAmount);
            if (bestPrice == null || candidate.finalPrice.compareTo(bestPrice) < 0) {
                bestPrice = candidate.finalPrice;
                bestPromo = promo;
                bestConsumption = candidate.consumeAmount;
                log.info("      -> NEW BEST PROMO");
            }
        }

        if (consumeQuota && bestPromo != null && bestConsumption > 0) {
            log.info("  LOCK & INCREMENT: promo {} | consumeAmount={}", bestPromo.getId(), bestConsumption);
            Optional<Promotion> lockedOpt = promotionRepository.findByIdForUpdate(bestPromo.getId());
            if (lockedOpt.isPresent()) {
                Promotion locked = lockedOpt.get();
                log.info("    BEFORE: usedQty={} | maxQty={}", locked.getUsedQuantity(), locked.getMaxQuantity());
                PromotionSelection lockedSelection = evaluatePromotionForItem(locked, item, basePrice, demandQuantity);
                if (lockedSelection != null && lockedSelection.consumeAmount > 0) {
                    bestPromo = locked;
                    bestPrice = lockedSelection.finalPrice;
                    bestConsumption = lockedSelection.consumeAmount;
                    locked.setUsedQuantity(normalizeUsedQuantity(locked.getUsedQuantity()) + bestConsumption);
                    log.info("    AFTER: usedQty={} | maxQty={}", locked.getUsedQuantity(), locked.getMaxQuantity());
                    savePromotionConsumingQuota(locked);  // This clears cache
                    log.info("    SAVED with cache evict");
                    // Notify owner if quota is exhausted
                    notifyQuotaExhausted(locked);
                } else {
                    log.info("    lockedSelection is null or consumeAmount <= 0");
                    bestPromo = null;
                    bestPrice = basePrice;
                }
            } else {
                log.info("    findByIdForUpdate returned empty");
            }
        }

        if (bestPrice == null) {
            bestPrice = basePrice;
        }

        BigDecimal discount = basePrice.subtract(bestPrice).max(BigDecimal.ZERO);

        return new CartItemPriceResponse(
                item.getProductId(),
                basePrice,
                bestPrice,
                discount,
                bestPromo == null ? null : bestPromo.getCode(),
                bestPromo == null ? null : bestPromo.getName(),
                bestPromo == null || bestPromo.getDiscountType() == null ? null : normalizeDiscountType(bestPromo).name(),
                demandQuantity
        );
    }

    private boolean appliesToItem(Promotion promo, CartItemPriceRequest.CartItem item) {
        if (promo == null || item == null || item.getProductId() == null) {
            return false;
        }
        Long productId = item.getProductId();
        Long categoryId = item.getCategoryId();

        List<PromotionTarget> targets = promo.getTargets();
        log.debug("      appliesToItem: promo {} has {} targets", promo.getId(), targets == null ? 0 : targets.size());
        if (targets != null) {
            for (PromotionTarget target : targets) {
                if (target == null || target.getTargetId() == null || target.getTargetType() == null) {
                    continue;
                }
                log.debug("        target: type={} id={}", target.getTargetType(), target.getTargetId());
                if (target.getTargetType() == PromotionTarget.TargetType.PRODUCT
                        && productId != null
                        && productId.equals(target.getTargetId())) {
                    log.debug("        -> MATCH on product");
                    return true;
                }
                if (target.getTargetType() == PromotionTarget.TargetType.CATEGORY
                        && categoryId != null
                        && categoryId.equals(target.getTargetId())) {
                    log.debug("        -> MATCH on category");
                    return true;
                }
            }
        }

        List<BundleItem> bundleItems = promo.getBundleItems();
        log.debug("      appliesToItem: promo {} has {} bundleItems", promo.getId(), bundleItems == null ? 0 : bundleItems.size());
        if (bundleItems != null && normalizeDiscountType(promo) == Promotion.DiscountType.BUNDLE) {
            for (BundleItem bundle : bundleItems) {
                if (bundle == null) {
                    continue;
                }
                Long mainId = bundle.getMainProductId() != null ? bundle.getMainProductId() : bundle.getProductId();
                log.debug("        bundle: mainId={}", mainId);
                if (productId.equals(mainId)) {
                    log.debug("        -> MATCH on bundle");
                    return true;
                }
            }
        }

        log.debug("      appliesToItem: promo {} does NOT apply to product {}", promo.getId(), productId);
        return false;
    }

    private PromotionSelection evaluatePromotionForItem(
            Promotion promo,
            CartItemPriceRequest.CartItem item,
            BigDecimal basePrice,
            int demandQuantity
    ) {
        if (promo == null || basePrice == null || demandQuantity <= 0 || !appliesToItem(promo, item)) {
            if (promo != null && item != null) {
                log.debug("      evaluatePromotionForItem: promo {} doesn't apply to item {}", 
                    promo.getId(), item.getProductId());
            }
            return null;
        }

        Promotion.DiscountType type = normalizeDiscountType(promo);
        if (type == null || !hasRemainingQuota(promo)) {
            log.debug("      evaluatePromotionForItem: promo {} type={} hasQuota={}", 
                promo.getId(), type, hasRemainingQuota(promo));
            return null;
        }

        BigDecimal value = promo.getDiscountValue() == null
                ? null
                : BigDecimal.valueOf(promo.getDiscountValue());

        int remaining = remainingQuotaForConsumption(promo);

        return switch (type) {
            case PERCENT -> {
                if (value == null) {
                    yield null;
                }
                int appliedQty = Math.min(demandQuantity, remaining);
                if (appliedQty <= 0) {
                    yield null;
                }
                BigDecimal percent = value.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal discountedUnit = basePrice.multiply(BigDecimal.ONE.subtract(percent)).max(BigDecimal.ZERO);
                BigDecimal finalTotal = discountedUnit.multiply(BigDecimal.valueOf(appliedQty))
                        .add(basePrice.multiply(BigDecimal.valueOf(demandQuantity - appliedQty)));
                BigDecimal finalUnit = finalTotal.divide(BigDecimal.valueOf(demandQuantity), 4, RoundingMode.HALF_UP);
                yield new PromotionSelection(finalUnit, appliedQty);
            }
            case FIXED, FIXED_AMOUNT -> {
                if (value == null) {
                    yield null;
                }
                int appliedQty = Math.min(demandQuantity, remaining);
                if (appliedQty <= 0) {
                    yield null;
                }
                BigDecimal discountedUnit = basePrice.subtract(value).max(BigDecimal.ZERO);
                BigDecimal finalTotal = discountedUnit.multiply(BigDecimal.valueOf(appliedQty))
                        .add(basePrice.multiply(BigDecimal.valueOf(demandQuantity - appliedQty)));
                BigDecimal finalUnit = finalTotal.divide(BigDecimal.valueOf(demandQuantity), 4, RoundingMode.HALF_UP);
                yield new PromotionSelection(finalUnit, appliedQty);
            }
            case BUNDLE, FREE_GIFT -> evaluateBundlePromotion(promo, item, basePrice, demandQuantity, remaining);
        };
    }

    private PromotionSelection evaluateBundlePromotion(
            Promotion promo,
            CartItemPriceRequest.CartItem item,
            BigDecimal basePrice,
            int demandQuantity,
            int remainingSets
    ) {
        if (promo == null || item == null || item.getProductId() == null || demandQuantity <= 0) {
            return null;
        }
        List<BundleItem> bundleItems = promo.getBundleItems();
        if (bundleItems == null || bundleItems.isEmpty()) {
            return null;
        }

        BundleItem bundle = bundleItems.stream()
                .filter(b -> b != null)
                .filter(b -> {
                    Long mainId = b.getMainProductId() != null ? b.getMainProductId() : b.getProductId();
                    return mainId != null && mainId.equals(item.getProductId());
                })
                .findFirst()
                .orElse(null);

        if (bundle == null) {
            return null;
        }

        int mainQty = safePositive(bundle.getMainQuantity() != null ? bundle.getMainQuantity() : bundle.getQuantity(), 1);
        int giftQty = safePositive(bundle.getGiftQuantity(), 1);
        int setSize = mainQty + giftQty;
        int possibleSets = setSize > 0 ? demandQuantity / setSize : 0;
        if (possibleSets <= 0) {
            return null;
        }

        // 🔧 FIX: Cap appliedSets theo remainingSets / giftQty để không vượt quá số sản phẩm được phép tặng
        // remainingSets = maxQuantity - usedQuantity = số sản phẩm còn được tặng
        // appliedSets = số lần áp dụng khuyến mãi = remainingSets / giftQty
        int maxAppliableSets = giftQty > 0 ? remainingSets / giftQty : 0;
        int appliedSets = Math.min(possibleSets, maxAppliableSets);
        if (appliedSets <= 0) {
            return null;
        }

        int freeUnits = appliedSets * giftQty;
        int chargeableUnits = Math.max(0, demandQuantity - freeUnits);
        BigDecimal finalTotal = basePrice.multiply(BigDecimal.valueOf(chargeableUnits));
        BigDecimal finalUnit = finalTotal.divide(BigDecimal.valueOf(demandQuantity), 4, RoundingMode.HALF_UP);
        return new PromotionSelection(finalUnit, appliedSets);
    }

    private int normalizeDemandQuantity(CartItemPriceRequest.CartItem item) {
        if (item == null || item.getQuantity() == null) {
            return 0;
        }
        return Math.max(0, item.getQuantity());
    }

    private int safePositive(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private Integer normalizeMaxQuantity(Integer maxQuantity) {
        if (maxQuantity == null || maxQuantity <= 0) {
            return null;
        }
        return maxQuantity;
    }

    private int normalizeUsedQuantity(Integer usedQuantity) {
        if (usedQuantity == null || usedQuantity < 0) {
            return 0;
        }
        return usedQuantity;
    }

    /**
     * 🔧 FIX: Xử lý logic reset usedQuantity khi maxQuantity thay đổi.
     * 
     * Quy tắc xử lý:
     * 1. Nếu new maxQuantity = 0/null (vô hạn) → RESET usedQuantity = 0
     *    - Vô hạn nghĩa là không cần đếm, nên reset
     * 
     * 2. Nếu old maxQuantity = 0/null (vô hạn) AND new maxQuantity > 0 (có hạn)
     *    → RESET usedQuantity = 0
     *    - Chuyển từ vô hạn sang có hạn, phải bắt đầu lại từ 0
     * 
     * 3. Nếu cả old và new đều > 0 (both có hạn) AND new < old usedQuantity
     *    → CAP usedQuantity = new maxQuantity
     *    - Đã dùng nhiều hơn giới hạn mới, cần cập nhật
     * 
     * 4. Các trường hợp khác → GIỮ usedQuantity cũ không thay đổi
     *    - Update quantity từ số hạn này sang số hạn khác (giống hoặc lớn hơn)
     * 
     * @param promotion Entity hiện tại (sẽ bị cập nhật)
     * @param oldMaxQuantity Giá trị maxQuantity cũ (trước khi change)
     * @param newMaxQuantity Giá trị maxQuantity mới (sau khi normalize)
     */
    private void handleUsedQuantityOnMaxQuantityChange(
            Promotion promotion, 
            Integer oldMaxQuantity, 
            Integer newMaxQuantity) {
        
        // Lấy usedQuantity hiện tại
        Integer currentUsedQuantity = normalizeUsedQuantity(promotion.getUsedQuantity());
        
        // Normalize các giá trị để so sánh
        Integer normalizedOldMax = normalizeMaxQuantity(oldMaxQuantity);
        
        log.info(
            "[handleUsedQuantityOnMaxQuantityChange] Processing quantity change: " +
            "oldMax={}, newMax={}, currentUsed={}, normalizedOldMax={}, normalizedNewMax={}",
            oldMaxQuantity, newMaxQuantity, currentUsedQuantity, normalizedOldMax, newMaxQuantity
        );
        
        // Quy tắc 1: Nếu new maxQuantity = null (vô hạn) → reset usedQuantity
        if (newMaxQuantity == null) {
            log.warn(
                "[handleUsedQuantityOnMaxQuantityChange] Rule 1 triggered: " +
                "New maxQuantity is unlimited (null), RESETTING usedQuantity from {} to 0",
                currentUsedQuantity
            );
            promotion.setUsedQuantity(0);
            return;
        }
        
        // Quy tắc 2: Chuyển từ vô hạn (old=null) sang có hạn (new>0)
        if (normalizedOldMax == null && newMaxQuantity > 0) {
            log.warn(
                "[handleUsedQuantityOnMaxQuantityChange] Rule 2 triggered: " +
                "Transition from unlimited to limited (newMax={}), RESETTING usedQuantity from {} to 0",
                newMaxQuantity, currentUsedQuantity
            );
            promotion.setUsedQuantity(0);
            return;
        }
        
        // Quy tắc 3: Cả old và new đều có hạn, nhưng new nhỏ hơn usedQuantity hiện tại
        if (normalizedOldMax != null && normalizedOldMax > 0 &&
            newMaxQuantity > 0 && newMaxQuantity < currentUsedQuantity) {
            
            log.warn(
                "[handleUsedQuantityOnMaxQuantityChange] Rule 3 triggered: " +
                "New max quantity ({}) is less than current used quantity ({}), CAPPING usedQuantity to {}",
                newMaxQuantity, currentUsedQuantity, newMaxQuantity
            );
            promotion.setUsedQuantity(newMaxQuantity);
            return;
        }
        
        // Quy tắc 4: Các trường hợp khác → giữ usedQuantity cũ
        log.info(
            "[handleUsedQuantityOnMaxQuantityChange] Rule 4 triggered: " +
            "No reset needed, keeping usedQuantity unchanged at {}",
            currentUsedQuantity
        );
        // usedQuantity giữ nguyên, không cần thay đổi
    }

    private boolean hasRemainingQuota(Promotion promo) {
        if (promo == null) {
            return false;
        }
        Integer max = normalizeMaxQuantity(promo.getMaxQuantity());
        if (max == null) {
            return true;
        }
        return normalizeUsedQuantity(promo.getUsedQuantity()) < max;
    }

    private boolean isQuotaExhausted(Promotion promo) {
        if (promo == null) {
            return false;
        }
        Integer max = normalizeMaxQuantity(promo.getMaxQuantity());
        if (max == null) {
            return false;  // No quota limit means not exhausted
        }
        return normalizeUsedQuantity(promo.getUsedQuantity()) >= max;
    }

    private int remainingQuotaForConsumption(Promotion promo) {
        Integer max = normalizeMaxQuantity(promo.getMaxQuantity());
        if (max == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, max - normalizeUsedQuantity(promo.getUsedQuantity()));
    }

    private Integer calculateRemainingQuantity(Promotion promotion) {
        Integer max = normalizeMaxQuantity(promotion == null ? null : promotion.getMaxQuantity());
        if (max == null) {
            return null;
        }
        return Math.max(0, max - normalizeUsedQuantity(promotion.getUsedQuantity()));
    }

    private void notifyQuotaExhausted(Promotion promotion) {
        if (promotion == null || promotion.getMaxQuantity() == null) {
            return;
        }
        
        Integer used = normalizeUsedQuantity(promotion.getUsedQuantity());
        Integer max = promotion.getMaxQuantity();
        
        // Only send notification when quota is exactly exhausted
        if (!used.equals(max)) {
            return;
        }
        
        try {
            // Schedule notification after transaction commit
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendQuotaExhaustedMessage(promotion);
                }
            });
        } catch (Exception ex) {
            log.warn("Failed to register transaction synchronization for quota exhausted notification", ex);
            // Try to send immediately as fallback
            sendQuotaExhaustedMessage(promotion);
        }
    }

    private void sendQuotaExhaustedMessage(Promotion promotion) {
        try {
            String messageContent = String.format(
                "🎁 Khuyến mãi \"%s\" (mã: %s) đã hết hạn ngạch sử dụng.\n" +
                "Đã áp dụng cho %d lần. Không thể áp dụng thêm nữa, giá sẽ quay về giá gốc cho các khách hàng tiếp theo.",
                promotion.getName(),
                promotion.getCode(),
                promotion.getMaxQuantity()
            );
            
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("senderId", SYSTEM_USER_ID);
            messagePayload.put("receiverId", OWNER_USER_ID);
            messagePayload.put("senderName", "BizFlow System");
            messagePayload.put("receiverName", "Owner");
            messagePayload.put("content", messageContent);
            
            String url = customerServiceUrl + "/api/messages";
            restTemplate.postForObject(url, messagePayload, Map.class);
            
            log.info("Sent quota exhausted notification for promotion: {}", promotion.getCode());
        } catch (Exception ex) {
            log.error("Failed to send quota exhausted notification for promotion: {}", promotion.getCode(), ex);
        }
    }

    private static final class PromotionSelection {
        private final BigDecimal finalPrice;
        private final int consumeAmount;

        private PromotionSelection(BigDecimal finalPrice, int consumeAmount) {
            this.finalPrice = finalPrice;
            this.consumeAmount = consumeAmount;
        }
    }

    private void sendNifiSignal(String eventType, PromotionDTO dto) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("promotion", dto);
        payload.put("timestamp", LocalDateTime.now());

        try {
            String url = nifiSignalUrl;
            if (url != null) {
                restTemplate.postForEntity(url, payload, Void.class);
            }
        } catch (org.springframework.web.client.RestClientException ex) {
            log.warn("Failed to send NiFi signal to {}", nifiSignalUrl, ex);
        }
    }

    @CacheEvict(cacheNames = "activePromotions", allEntries = true)
    private void savePromotionConsumingQuota(Promotion promotion) {
        promotionRepository.save(promotion);
    }
}
