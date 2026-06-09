package com.example.promotion.controller;

import com.example.promotion.dto.CartItemPriceRequest;
import com.example.promotion.dto.CartItemPriceResponse;
import com.example.promotion.dto.PromotionDTO;
import com.example.promotion.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // GET /api/v1/promotions
    @GetMapping
    public ResponseEntity<List<PromotionDTO>> getAllPromotions(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "targetId", required = false) Long targetId,
            @RequestParam(value = "active", required = false) Boolean active
    ) {
        try {
            boolean hasFilter = (search != null && !search.isBlank())
                    || (type != null && !type.isBlank())
                    || (targetType != null && !targetType.isBlank())
                    || targetId != null
                    || active != null;
            if (hasFilter) {
                return ResponseEntity.ok(
                        promotionService.searchPromotions(search, type, targetType, targetId, active)
                );
            }
            return ResponseEntity.ok(promotionService.getAllPromotions());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // GET /api/v1/promotions/active
    @GetMapping("/active")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        try {
            return ResponseEntity.ok(promotionService.getActivePromotions());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // GET /api/v1/promotions/code/{code}
    @GetMapping("/code/{code}")
    public ResponseEntity<PromotionDTO> getByCode(@PathVariable String code) {
        PromotionDTO dto = promotionService.getPromotionByCode(code);
        return dto != null
                ? ResponseEntity.ok(dto)
                : ResponseEntity.notFound().build();
    }

    // GET /api/v1/promotions/generate-code?name=...
    @GetMapping("/generate-code")
    public ResponseEntity<?> generateCode(@RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(java.util.Map.of("code", promotionService.generatePromotionCode(name)));
    }

    // POST /api/v1/promotions
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PromotionDTO> createPromotion(@RequestBody PromotionDTO dto) {
        PromotionDTO created = promotionService.createPromotion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/v1/promotions/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PromotionDTO> updatePromotion(
            @PathVariable Long id,
            @RequestBody PromotionDTO dto
    ) {
        PromotionDTO updated = promotionService.updatePromotion(id, dto);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/v1/promotions/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deletePromotion(
            @PathVariable Long id
    ) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/promotions/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deactivatePromotion(@PathVariable Long id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/promotions/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> activatePromotion(@PathVariable Long id) {
        promotionService.activatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/promotions/sync
    @PostMapping("/sync")
    public ResponseEntity<?> syncPromotion(@RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.createPromotion(dto));
    }

    // POST /api/v1/promotions/calculate-prices
    @PostMapping("/calculate-prices")
    public ResponseEntity<List<CartItemPriceResponse>> calculateCartPrices(
            @RequestBody CartItemPriceRequest request
    ) {
        try {
            List<CartItemPriceResponse> prices = promotionService.calculateCartItemPrices(request);
            return ResponseEntity.ok(prices);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/v1/promotions/calculate-prices/consume
    @PostMapping("/calculate-prices/consume")
    public ResponseEntity<List<CartItemPriceResponse>> calculateAndConsumeCartPrices(
            @RequestBody CartItemPriceRequest request
    ) {
        try {
            List<CartItemPriceResponse> prices = promotionService.calculateAndConsumeCartItemPrices(request);
            return ResponseEntity.ok(prices);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/v1/promotions/validate/{code}
    @GetMapping("/validate/{code}")
    public ResponseEntity<PromotionDTO> validateCode(@PathVariable String code) {
        PromotionDTO promo = promotionService.getActivePromotionByCode(code);
        return (promo != null) ? ResponseEntity.ok(promo) : ResponseEntity.notFound().build();
    }
}
