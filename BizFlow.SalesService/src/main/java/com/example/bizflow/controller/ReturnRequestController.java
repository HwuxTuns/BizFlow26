package com.example.bizflow.controller;

import com.example.bizflow.dto.CreateReturnRequestDTO;
import com.example.bizflow.dto.UpdateReturnStatusDTO;
import com.example.bizflow.entity.Order;
import com.example.bizflow.entity.OrderItem;
import com.example.bizflow.entity.ReturnRequest;
import com.example.bizflow.integration.CustomerClient;
import com.example.bizflow.integration.InventoryClient;
import com.example.bizflow.repository.OrderRepository;
import com.example.bizflow.repository.ReturnRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/return-requests")
public class ReturnRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ReturnRequestController.class);

    /**
     * Số ngày tối đa cho phép đổi/trả kể từ ngày tạo đơn hàng
     */
    private static final int MAX_RETURN_DAYS = 30;

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;

    public ReturnRequestController(ReturnRequestRepository returnRequestRepository,
                                   OrderRepository orderRepository,
                                   ObjectMapper objectMapper,
                                   InventoryClient inventoryClient,
                                   CustomerClient customerClient) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.inventoryClient = inventoryClient;
        this.customerClient = customerClient;
    }

    /**
     * Khách hàng tạo yêu cầu đổi/trả
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createReturnRequest(@RequestBody CreateReturnRequestDTO dto) {
        if (dto.getOrderId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã đơn hàng là bắt buộc"));
        }
        if (dto.getProductId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn sản phẩm cần đổi/trả"));
        }
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn lý do đổi/trả"));
        }

        // --- Validate đơn hàng tồn tại ---
        Order order = orderRepository.findById(dto.getOrderId()).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Đơn hàng không tồn tại"));
        }

        // --- Validate trạng thái đơn hàng phải PAID hoặc RECEIVED ---
        String orderStatus = order.getStatus() != null ? order.getStatus().trim().toUpperCase() : "";
        if (!"PAID".equals(orderStatus) && !"RECEIVED".equals(orderStatus)) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "Chỉ đơn hàng đã thanh toán (PAID) hoặc đã nhận hàng (RECEIVED) mới được đổi/trả. Trạng thái hiện tại: " + orderStatus));
        }

        // --- Validate thời hạn đổi/trả (30 ngày) ---
        if (order.getCreatedAt() != null) {
            long daysSinceOrder = ChronoUnit.DAYS.between(order.getCreatedAt(), LocalDateTime.now());
            if (daysSinceOrder > MAX_RETURN_DAYS) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Đã quá thời hạn đổi/trả hàng (" + MAX_RETURN_DAYS + " ngày). Đơn hàng được tạo cách đây " + daysSinceOrder + " ngày."));
            }
        }

        // --- Validate phân quyền ---
        if (order.getUserId() != null && !order.getUserId().equals(dto.getCreatedBy())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Từ chối phân quyền: Chỉ tài khoản (nhân viên/khách hàng) thực hiện giao dịch này mới được phép tạo yêu cầu đổi/trả."));
        }

        // --- Validate số lượng đổi/trả không vượt số lượng đã mua ---
        int totalBought = 0;
        for (OrderItem item : order.getItems()) {
            if (item.getProductId().equals(dto.getProductId())) {
                totalBought += item.getQuantity();
            }
        }
        if (totalBought == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sản phẩm không thuộc đơn hàng này"));
        }

        int requestedQty = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;

        List<ReturnRequest> existingRequests = returnRequestRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
        int alreadyReturned = 0;
        for (ReturnRequest existing : existingRequests) {
            if (existing.getProductId().equals(dto.getProductId())
                && !"REJECTED".equalsIgnoreCase(existing.getStatus())) {
                alreadyReturned += existing.getQuantity();
            }
        }

        if (alreadyReturned + requestedQty > totalBought) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số lượng đổi/trả vượt quá số lượng đã mua. Bạn đã mua " + totalBought + ", đã đổi/trả " + alreadyReturned + "."));
        }

        // --- Tính refundAmount ---
        BigDecimal unitPrice = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getProductId().equals(dto.getProductId()) && item.getPrice() != null) {
                unitPrice = item.getPrice();
                break;
            }
        }
        BigDecimal refundAmount = unitPrice.multiply(BigDecimal.valueOf(requestedQty));

        // --- Tạo yêu cầu ---
        ReturnRequest request = new ReturnRequest();
        request.setOrderId(dto.getOrderId());
        request.setInvoiceNumber(dto.getInvoiceNumber());
        request.setCustomerId(dto.getCustomerId());
        request.setCustomerName(dto.getCustomerName());
        request.setCustomerPhone(dto.getCustomerPhone());
        request.setProductId(dto.getProductId());
        request.setProductName(dto.getProductName());
        request.setQuantity(requestedQty);
        request.setReason(dto.getReason());
        request.setReasonDetail(dto.getReasonDetail());
        request.setRequestType(dto.getRequestType() != null ? dto.getRequestType() : "REFUND");
        request.setCreatedBy(dto.getCreatedBy());
        request.setRefundAmount(refundAmount);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        // Serialize evidence images to JSON
        if (dto.getEvidenceImages() != null && !dto.getEvidenceImages().isEmpty()) {
            try {
                request.setEvidenceImages(objectMapper.writeValueAsString(dto.getEvidenceImages()));
            } catch (Exception e) {
                // ignore serialization error
            }
        }

        ReturnRequest saved = returnRequestRepository.save(request);

        return ResponseEntity.ok(Map.of(
                "message", "Yêu cầu đổi/trả đã được gửi thành công",
                "id", saved.getId(),
                "status", saved.getStatus()
        ));
    }

    /**
     * Lấy danh sách tất cả yêu cầu (Admin)
     */
    @GetMapping
    public ResponseEntity<List<ReturnRequest>> listReturnRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        String trimmedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        String trimmedStatus = status != null && !status.isBlank() ? status.trim().toUpperCase() : null;

        List<ReturnRequest> results = returnRequestRepository.searchReturnRequests(trimmedStatus, trimmedKeyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Xem chi tiết yêu cầu
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReturnRequest(@PathVariable Long id) {
        return returnRequestRepository.findById(id)
                .map(req -> ResponseEntity.ok((Object) req))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Lấy yêu cầu theo đơn hàng
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<ReturnRequest>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(returnRequestRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
    }

    /**
     * Lấy yêu cầu theo khách hàng
     */
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<ReturnRequest>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(returnRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    /**
     * Lấy yêu cầu do nhân viên tạo
     */
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<ReturnRequest>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(returnRequestRepository.findByCreatedByOrderByCreatedAtDesc(employeeId));
    }

    /**
     * Admin duyệt / từ chối yêu cầu
     * Khi COMPLETED: hoàn kho + xử lý điểm tích lũy
     */
    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @RequestBody UpdateReturnStatusDTO dto) {
        ReturnRequest request = returnRequestRepository.findById(id).orElse(null);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái là bắt buộc"));
        }

        String newStatus = dto.getStatus().trim().toUpperCase();
        if (!List.of("PENDING", "APPROVED", "REJECTED", "COMPLETED").contains(newStatus)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái không hợp lệ"));
        }

        String oldStatus = request.getStatus();

        request.setStatus(newStatus);
        if (dto.getAdminNote() != null) {
            request.setAdminNote(dto.getAdminNote());
        }
        request.setUpdatedAt(LocalDateTime.now());

        // --- Khi hoàn tất (COMPLETED): hoàn kho + trừ điểm ---
        if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(oldStatus)) {
            // 1. Hoàn kho: gọi InventoryClient với quantity âm (sẽ addToShelf)
            try {
                InventoryClient.SaleItem saleItem = new InventoryClient.SaleItem();
                saleItem.setProductId(request.getProductId());
                saleItem.setQuantity(-request.getQuantity()); // Quantity âm = hoàn kho
                saleItem.setUnitPrice(request.getRefundAmount() != null
                        ? request.getRefundAmount().divide(BigDecimal.valueOf(request.getQuantity()), java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

                List<InventoryClient.SaleItem> items = new ArrayList<>();
                items.add(saleItem);
                inventoryClient.applySale(items, request.getOrderId(), null);
                logger.info("Hoàn kho thành công cho return request #{}: productId={}, qty={}",
                        request.getId(), request.getProductId(), request.getQuantity());
            } catch (Exception e) {
                logger.warn("Lỗi hoàn kho cho return request #{}: {}", request.getId(), e.getMessage());
            }

            // 2. Trừ điểm tích lũy (nếu có customerId và refundAmount)
            if (request.getCustomerId() != null && request.getRefundAmount() != null
                    && request.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    // Tính số điểm cần trừ: ~1 điểm / 1000 VND
                    int pointsToDeduct = request.getRefundAmount()
                            .divide(BigDecimal.valueOf(1000), java.math.RoundingMode.DOWN)
                            .intValue();
                    if (pointsToDeduct > 0) {
                        String ref = "RETURN_DEDUCT_" + request.getId();
                        customerClient.redeemPoints(request.getCustomerId(), pointsToDeduct, ref);
                        logger.info("Trừ {} điểm cho khách #{} do đổi/trả #{}", pointsToDeduct, request.getCustomerId(), request.getId());
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi trừ điểm cho return request #{}: {}", request.getId(), e.getMessage());
                }
            }
        }

        ReturnRequest updated = returnRequestRepository.save(request);
        return ResponseEntity.ok(updated);
    }
}
