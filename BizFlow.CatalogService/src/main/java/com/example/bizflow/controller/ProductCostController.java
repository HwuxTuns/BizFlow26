package com.example.bizflow.controller;

import com.example.bizflow.dto.ProductCostHistoryDTO;
import com.example.bizflow.dto.ProductCostUpdateRequest;
import com.example.bizflow.dto.NewProductPurchaseRequest;
import com.example.bizflow.entity.ProductCost;
import com.example.bizflow.service.ProductCostService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-costs")
public class ProductCostController {

    private final ProductCostService productCostService;

    public ProductCostController(ProductCostService productCostService) {
        this.productCostService = productCostService;
    }

    /**
     * Lấy giá vốn hiện tại của sản phẩm
     */
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> getProductCost(@PathVariable Long productId) {
        return productCostService.getProductCost(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cập nhật giá vốn khi nhập hàng
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> updateCostPrice(@RequestBody ProductCostUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            ProductCost result = productCostService.updateCostPrice(request, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Nhập hàng với giá vốn mới (tạo lịch sử)
     */
    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> recordPurchase(@RequestBody ProductCostUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            var history = productCostService.recordPurchase(
                    request.getProductId(),
                    request.getCostPrice(),
                    request.getQuantity(),
                    request.getNote(),
                    userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Nhap hang voi san pham moi (tao san pham, cap nhat gia von va ton kho)
     */
    @PostMapping("/purchase/new-product")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<?> recordPurchaseWithNewProduct(@RequestBody NewProductPurchaseRequest request) {
        try {
            Long userId = getCurrentUserId();
            var history = productCostService.createProductAndPurchase(request, userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử giá vốn của một sản phẩm
     */
    @GetMapping("/{productId}/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<ProductCostHistoryDTO>> getProductCostHistory(@PathVariable Long productId) {
        List<ProductCostHistoryDTO> history = productCostService.getCostHistory(productId);
        return ResponseEntity.ok(history);
    }

    /**
     * Lấy tất cả lịch sử nhập hàng
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<ProductCostHistoryDTO>> getAllCostHistory() {
        List<ProductCostHistoryDTO> history = productCostService.getAllCostHistory();
        return ResponseEntity.ok(history);
    }

    /**
     * Lấy lịch sử theo khoảng thời gian
     */
    @GetMapping("/history/range")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<ProductCostHistoryDTO>> getCostHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long productId) {

        List<ProductCostHistoryDTO> history;
        if (productId != null) {
            history = productCostService.getProductCostHistoryByDateRange(productId, startDate, endDate);
        } else {
            history = productCostService.getCostHistoryByDateRange(startDate, endDate);
        }
        return ResponseEntity.ok(history);
    }

    /**
     * Khởi tạo giá vốn từ bảng products (chạy 1 lần khi migration)
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initializeCosts() {
        try {
            productCostService.initializeCostsFromProducts();
            return ResponseEntity.ok(Map.of("message", "Đã khởi tạo giá vốn từ bảng products"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
            Object userId = principal.get("user_id");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }
}
