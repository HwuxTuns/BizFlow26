package com.example.bizflow.controller;

import com.example.bizflow.dto.InventoryAdjustRequest;
import com.example.bizflow.dto.InventoryAlertDTO;
import com.example.bizflow.dto.InventoryHistoryItem;
import com.example.bizflow.dto.InventoryReceiptRequest;
import com.example.bizflow.dto.InventoryReceiptResponse;
import com.example.bizflow.dto.InventoryStockOutRequest;
import com.example.bizflow.entity.InventoryStock;
import com.example.bizflow.entity.InventoryTransaction;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.repository.InventoryStockRepository;
import com.example.bizflow.repository.InventoryTransactionRepository;
import com.example.bizflow.service.InventoryAlertService;
import com.example.bizflow.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private static final DateTimeFormatter HISTORY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CatalogClient catalogClient;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryService inventoryService;
    private final InventoryAlertService inventoryAlertService;

    public InventoryController(CatalogClient catalogClient,
            InventoryTransactionRepository inventoryTransactionRepository,
            InventoryStockRepository inventoryStockRepository,
            InventoryService inventoryService,
            InventoryAlertService inventoryAlertService) {
        this.catalogClient = catalogClient;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryService = inventoryService;
        this.inventoryAlertService = inventoryAlertService;
    }

    // ==================== XEM TỒN KHO ====================
    @GetMapping("/stock/{productId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getStock(@PathVariable Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        int stock = inventoryService.getAvailableStock(productId);
        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product != null ? product.getName() : null);
        result.put("stock", stock);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getAllStock() {
        List<CatalogClient.ProductSnapshot> products = catalogClient.getProducts();
        List<Map<String, Object>> result = new ArrayList<>();

        for (CatalogClient.ProductSnapshot product : products) {
            if (product == null || product.getId() == null) {
                continue;
            }
            if (product.getStatus() != null && !"active".equalsIgnoreCase(product.getStatus())) {
                continue;
            }
            int stock = inventoryStockRepository.findByProductId(product.getId())
                    .map(InventoryStock::getStock)
                    .orElse(0);

            Map<String, Object> item = new HashMap<>();
            item.put("productId", product.getId());
            item.put("productCode", product.getCode());
            item.put("productName", product.getName());
            item.put("categoryId", product.getCategoryId());
            item.put("stock", stock);
            item.put("unit", product.getUnit());
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== NHẬP KHO ====================
    @PostMapping("/receipts")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> receiveStock(@RequestBody InventoryReceiptRequest request) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().body("Product is required");
        }

        InventoryTransaction tx = inventoryService.receiveStock(
                request.getProductId(),
                request.getQuantity() == null ? 0 : request.getQuantity(),
                request.getUnitPrice(),
                request.getNote(),
                request.getUserId()
        );

        int stock = inventoryService.getAvailableStock(request.getProductId());

        return ResponseEntity.ok(new InventoryReceiptResponse(
                request.getProductId(),
                stock,
                tx.getId()
        ));
    }

    // ==================== ĐIỀU CHỈNH KHO (KIỂM KHO) ====================
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<?> adjustStock(@RequestBody InventoryAdjustRequest request) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        if (request.getNewQuantity() == null) {
            return ResponseEntity.badRequest().body("New quantity is required");
        }

        try {
            InventoryTransaction tx = inventoryService.adjustStock(
                    request.getProductId(),
                    request.getNewQuantity(),
                    request.getReason(),
                    request.getNote(),
                    request.getUserId()
            );

            int newStock = inventoryService.getAvailableStock(request.getProductId());

            Map<String, Object> result = new HashMap<>();
            result.put("productId", request.getProductId());
            result.put("newStock", newStock);
            result.put("transactionId", tx != null ? tx.getId() : null);
            result.put("message", tx != null ? "Stock adjusted successfully" : "No change needed");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== XUẤT KHO THỦ CÔNG ====================
    @PostMapping("/stock-out")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<?> manualStockOut(@RequestBody InventoryStockOutRequest request) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be > 0");
        }

        try {
            InventoryTransaction tx = inventoryService.manualStockOut(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getReason(),
                    request.getNote(),
                    request.getUserId()
            );

            int newStock = inventoryService.getAvailableStock(request.getProductId());

            Map<String, Object> result = new HashMap<>();
            result.put("productId", request.getProductId());
            result.put("quantityRemoved", request.getQuantity());
            result.put("newStock", newStock);
            result.put("transactionId", tx.getId());
            result.put("message", "Stock removed successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== LỊCH SỬ GIAO DỊCH ====================
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getHistory(@RequestParam(name = "productId") Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product is required");
        }

        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);
        String productName = product == null ? null : product.getName();
        List<InventoryTransaction> history = inventoryTransactionRepository
                .findTop50ByProductIdOrderByCreatedAtDesc(productId);
        List<InventoryHistoryItem> items = new ArrayList<>();

        for (InventoryTransaction tx : history) {
            String createdAt = tx.getCreatedAt() == null ? null : tx.getCreatedAt().format(HISTORY_FORMAT);
            items.add(new InventoryHistoryItem(
                    tx.getId(),
                    tx.getProductId(),
                    productName,
                    tx.getTransactionType() == null ? null : tx.getTransactionType().name(),
                    tx.getQuantity(),
                    tx.getUnitPrice(),
                    tx.getNote(),
                    createdAt
            ));
        }

        return ResponseEntity.ok(items);
    }

    // ==================== THÔNG BÁO TỒN KHO & TỒN KỆ ====================
    /**
     * API lấy tất cả thông báo cảnh báo tồn kho và tồn kệ
     * 
     * @param lastChecked Ngày kiểm tra cuối (format: yyyy-MM-dd). 
     *                    Nếu null hoặc < today → hiển thị thông báo kho.
     *                    Thông báo kệ luôn hiển thị realtime.
     * 
     * @return List<InventoryAlertDTO> danh sách thông báo
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<InventoryAlertDTO>> getInventoryAlerts(
            @RequestParam(required = false) String lastChecked) {
        
        LocalDate lastCheckedDate = null;
        if (lastChecked != null && !lastChecked.trim().isEmpty()) {
            try {
                lastCheckedDate = LocalDate.parse(lastChecked);
            } catch (Exception e) {
                // Invalid format, treat as null
            }
        }

        List<InventoryAlertDTO> alerts = inventoryAlertService.getAllAlerts(lastCheckedDate);
        return ResponseEntity.ok(alerts);
    }
}
