package com.example.bizflow.controller;

import com.example.bizflow.dto.CreateWarrantyRequestDTO;
import com.example.bizflow.dto.UpdateWarrantyStatusDTO;
import com.example.bizflow.entity.Order;
import com.example.bizflow.entity.OrderItem;
import com.example.bizflow.entity.WarrantyRequest;
import com.example.bizflow.entity.WarrantyHistory;
import com.example.bizflow.repository.OrderRepository;
import com.example.bizflow.repository.WarrantyHistoryRepository;
import com.example.bizflow.repository.WarrantyRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/warranty-requests")
public class WarrantyRequestController {

    private static final Logger logger = LoggerFactory.getLogger(WarrantyRequestController.class);

    /**
     * Số ngày bảo hành mặc định nếu sản phẩm không có thông tin riêng
     */
    private static final int DEFAULT_WARRANTY_DAYS = 30;

    private final WarrantyRequestRepository warrantyRequestRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final com.example.bizflow.integration.CatalogClient catalogClient;
    private final WarrantyHistoryRepository warrantyHistoryRepository;

    public WarrantyRequestController(WarrantyRequestRepository warrantyRequestRepository,
                                     OrderRepository orderRepository,
                                     ObjectMapper objectMapper,
                                     com.example.bizflow.integration.CatalogClient catalogClient,
                                     WarrantyHistoryRepository warrantyHistoryRepository) {
        this.warrantyRequestRepository = warrantyRequestRepository;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.catalogClient = catalogClient;
        this.warrantyHistoryRepository = warrantyHistoryRepository;
    }

    // ===================== TẠO YÊU CẦU BẢO HÀNH =====================

    @PostMapping
    @Transactional
    public ResponseEntity<?> createWarrantyRequest(@RequestBody CreateWarrantyRequestDTO dto) {
        logger.info("Tạo yêu cầu bảo hành cho đơn #{}, sản phẩm #{}", dto.getOrderId(), dto.getProductId());

        // --- Validate đơn hàng tồn tại ---
        Order order = orderRepository.findById(dto.getOrderId()).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không tìm thấy đơn hàng #" + dto.getOrderId()
            ));
        }

        // --- Validate trạng thái đơn: chỉ cho bảo hành đơn đã thanh toán/đã nhận ---
        String orderStatus = order.getStatus();
        if (orderStatus == null || (!orderStatus.equalsIgnoreCase("PAID")
                && !orderStatus.equalsIgnoreCase("RECEIVED")
                && !orderStatus.equalsIgnoreCase("COMPLETED"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Đơn hàng chưa thanh toán hoặc đã bị hủy, không thể gửi yêu cầu bảo hành"
            ));
        }

        // --- Validate sản phẩm có trong đơn hàng ---
        boolean productInOrder = false;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() != null && item.getProductId().equals(dto.getProductId())) {
                    productInOrder = true;
                    break;
                }
            }
        }
        if (!productInOrder) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Sản phẩm #" + dto.getProductId() + " không có trong đơn hàng này"
            ));
        }

        // --- Kiểm tra trùng: không cho gửi nếu đã có yêu cầu BH đang xử lý cho cùng SP trong cùng đơn ---
        List<String> activeStatuses = List.of("PENDING", "APPROVED", "REPAIRING");
        List<WarrantyRequest> existing = warrantyRequestRepository
                .findByOrderIdAndProductIdAndStatusIn(dto.getOrderId(), dto.getProductId(), activeStatuses);
        if (!existing.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Sản phẩm này đã có yêu cầu bảo hành đang được xử lý (mã #" + existing.get(0).getId() + ")"
            ));
        }

        // --- Lấy thông tin bảo hành từ sản phẩm (qua CatalogService) ---
        int warrantyDays = DEFAULT_WARRANTY_DAYS;
        try {
            com.example.bizflow.integration.CatalogClient.ProductSnapshot product = catalogClient.getProduct(dto.getProductId());
            if (product != null && product.getWarrantyPeriod() != null) {
                warrantyDays = product.getWarrantyPeriod();
            }
        } catch (Exception e) {
            logger.warn("Lỗi khi lấy thông tin bảo hành từ CatalogService, sử dụng mặc định 30 ngày", e);
        }

        // --- Tính ngày mua và kiểm tra thời hạn bảo hành ---
        LocalDateTime purchaseDate = order.getCreatedAt();
        LocalDateTime warrantyExpiry = purchaseDate.plusDays(warrantyDays);

        long daysSincePurchase = ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
        if (daysSincePurchase > warrantyDays) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Sản phẩm đã hết hạn bảo hành (mua cách đây " + daysSincePurchase
                            + " ngày, thời gian bảo hành cho phép là " + warrantyDays + " ngày)",
                    "purchaseDate", purchaseDate.toString(),
                    "warrantyExpiry", warrantyExpiry.toString()
            ));
        }

        // --- Validate mô tả lỗi ---
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Vui lòng mô tả lỗi / vấn đề của sản phẩm"
            ));
        }

        // --- Tạo entity ---
        WarrantyRequest warranty = new WarrantyRequest();
        warranty.setOrderId(dto.getOrderId());
        warranty.setInvoiceNumber(dto.getInvoiceNumber() != null ? dto.getInvoiceNumber() : order.getInvoiceNumber());
        warranty.setProductId(dto.getProductId());
        warranty.setProductName(dto.getProductName());
        warranty.setCustomerId(dto.getCustomerId() != null ? dto.getCustomerId() : order.getCustomerId());
        warranty.setCustomerName(dto.getCustomerName());
        warranty.setCustomerPhone(dto.getCustomerPhone() != null ? dto.getCustomerPhone() : order.getCustomerPhone());
        warranty.setDescription(dto.getDescription().trim());
        warranty.setWarrantyDays(warrantyDays);
        warranty.setPurchaseDate(purchaseDate);
        warranty.setWarrantyExpiry(warrantyExpiry);
        warranty.setStatus("PENDING");
        warranty.setCreatedBy(dto.getCreatedBy());

        // Lưu evidence images dưới dạng JSON
        if (dto.getEvidenceImages() != null && !dto.getEvidenceImages().isEmpty()) {
            try {
                warranty.setEvidenceImages(objectMapper.writeValueAsString(dto.getEvidenceImages()));
            } catch (Exception e) {
                logger.warn("Lỗi serialize evidence images", e);
            }
        }

        WarrantyRequest saved = warrantyRequestRepository.save(warranty);
        logger.info("Đã tạo yêu cầu bảo hành #{} thành công", saved.getId());
        return ResponseEntity.ok(saved);
    }

    // ===================== CẬP NHẬT TRẠNG THÁI (Admin) =====================

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateWarrantyStatusDTO dto) {
        logger.info("Admin cập nhật trạng thái bảo hành #{} -> {}", id, dto.getStatus());

        WarrantyRequest warranty = warrantyRequestRepository.findById(id).orElse(null);
        if (warranty == null) {
            return ResponseEntity.notFound().build();
        }

        // Validate trạng thái hợp lệ
        List<String> validStatuses = List.of("PENDING", "APPROVED", "REJECTED", "REPAIRING", "COMPLETED");
        if (dto.getStatus() == null || !validStatuses.contains(dto.getStatus().toUpperCase())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Trạng thái không hợp lệ. Cho phép: " + validStatuses
            ));
        }

        warranty.setStatus(dto.getStatus().toUpperCase());

        if (dto.getAdminNote() != null && !dto.getAdminNote().trim().isEmpty()) {
            warranty.setAdminNote(dto.getAdminNote().trim());
        }

        WarrantyRequest updated = warrantyRequestRepository.save(warranty);

        // --- Lưu lịch sử bảo hành ---
        WarrantyHistory history = new WarrantyHistory();
        history.setWarrantyRequestId(updated.getId());
        history.setStatus(updated.getStatus());
        history.setNote(dto.getAdminNote());
        history.setUpdatedBy("Admin/System"); // Giả định admin, có thể lấy từ SecurityContext nếu có
        warrantyHistoryRepository.save(history);

        logger.info("Đã cập nhật bảo hành #{} sang trạng thái {}", id, updated.getStatus());
        return ResponseEntity.ok(updated);
    }

    // ===================== LẤY DANH SÁCH =====================

    @GetMapping
    public ResponseEntity<List<WarrantyRequest>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        List<WarrantyRequest> results;

        if ((status != null && !status.isEmpty()) || (keyword != null && !keyword.isEmpty())) {
            String statusParam = (status != null && !status.isEmpty()) ? status.toUpperCase() : null;
            String keywordParam = (keyword != null && !keyword.isEmpty()) ? keyword : null;
            results = warrantyRequestRepository.searchWarrantyRequests(statusParam, keywordParam);
        } else {
            results = warrantyRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        return ResponseEntity.ok(results);
    }

    // ===================== LẤY THEO ID =====================

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        WarrantyRequest warranty = warrantyRequestRepository.findById(id).orElse(null);
        if (warranty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(warranty);
    }

    // ===================== LẤY THEO NHÂN VIÊN (createdBy) =====================

    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<WarrantyRequest>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(
                warrantyRequestRepository.findByCreatedByOrderByCreatedAtDesc(employeeId)
        );
    }

    // ===================== LẤY THEO KHÁCH HÀNG =====================

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<WarrantyRequest>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(
                warrantyRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
        );
    }

    // ===================== LẤY THEO ĐƠN HÀNG =====================

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<WarrantyRequest>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                warrantyRequestRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
        );
    }

    // ===================== LẤY LỊCH SỬ XỬ LÝ =====================

    @GetMapping("/{id}/history")
    public ResponseEntity<List<WarrantyHistory>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(
                warrantyHistoryRepository.findByWarrantyRequestIdOrderByCreatedAtDesc(id)
        );
    }
}
