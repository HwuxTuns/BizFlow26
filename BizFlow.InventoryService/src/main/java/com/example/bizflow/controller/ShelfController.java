package com.example.bizflow.controller;

import com.example.bizflow.dto.ShelfMoveRequest;
import com.example.bizflow.dto.ShelfStockResponse;
import com.example.bizflow.entity.Shelf;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.service.ShelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/shelves")
@CrossOrigin(origins = "*")
public class ShelfController {

    private final ShelfService shelfService;
    private final CatalogClient catalogClient;

    public ShelfController(ShelfService shelfService, CatalogClient catalogClient) {
        this.shelfService = shelfService;
        this.catalogClient = catalogClient;
    }

    // ==================== OWNER: ĐƯA HÀNG LÊN KỆ ====================
    @PostMapping("/move-to-shelf")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> moveToShelf(@RequestBody ShelfMoveRequest request, Authentication auth) {
        Long userId = getUserId(auth);
        shelfService.moveToShelf(request.getProductId(), request.getQuantity(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product moved to shelf successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== OWNER: BỎ HÀNG KHỎI KỆ ====================
    @PostMapping("/remove-from-shelf")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> removeFromShelf(@RequestBody ShelfMoveRequest request, Authentication auth) {
        Long userId = getUserId(auth);
        shelfService.removeFromShelf(request.getProductId(), request.getQuantity(), userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product removed from shelf successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== XEM TẤT CẢ SẢN PHẨM TRÊN KỆ ====================
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getAllShelves() {
        List<Shelf> shelves = shelfService.getAllShelves();
        List<ShelfStockResponse> responses = new ArrayList<>();

        for (Shelf shelf : shelves) {
            if (shelf.getProductId() == null) {
                continue;
            }

            // CHỈ trả về sản phẩm có quantity > 0 (đang thực sự bán)
            Integer qty = shelf.getQuantity();
            int quantity = qty != null ? qty : 0;
            if (quantity <= 0) {
                continue; // Bỏ qua sản phẩm không còn hàng trên kệ
            }

            CatalogClient.ProductSnapshot product = catalogClient.getProduct(shelf.getProductId());
            if (product == null) {
                continue;
            }

            String alertLevel = shelfService.getAlertLevel(quantity);

            ShelfStockResponse response = new ShelfStockResponse(
                    shelf.getId(),
                    shelf.getProductId(),
                    product.getCode(),
                    product.getName(),
                    product.getCategoryId(),
                    quantity,
                    alertLevel,
                    product.getPrice(),
                    product.getUnit()
            );
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    }

    // ==================== XEM SẢN PHẨM CỤ THỂ TRÊN KỆ ====================
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getShelfByProduct(@PathVariable Long productId) {
        int quantity = shelfService.getShelfQuantity(productId);
        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product != null ? product.getName() : null);
        result.put("productCode", product != null ? product.getCode() : null);
        result.put("quantity", quantity);
        result.put("alertLevel", shelfService.getAlertLevel(quantity));

        return ResponseEntity.ok(result);
    }

    // ==================== BÁO CÁO KỆ HÀNG CẢNH BÁO ====================
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> getLowStockShelves(@RequestParam(defaultValue = "10") int threshold) {
        List<Shelf> shelves = shelfService.getLowStockShelves(threshold);
        List<ShelfStockResponse> responses = new ArrayList<>();

        for (Shelf shelf : shelves) {
            if (shelf.getProductId() == null) {
                continue;
            }

            CatalogClient.ProductSnapshot product = catalogClient.getProduct(shelf.getProductId());
            if (product == null) {
                continue;
            }

            Integer qty = shelf.getQuantity();
            int quantity = qty != null ? qty : 0;
            String alertLevel = shelfService.getAlertLevel(quantity);

            ShelfStockResponse response = new ShelfStockResponse(
                    shelf.getId(),
                    shelf.getProductId(),
                    product.getCode(),
                    product.getName(),
                    product.getCategoryId(),
                    quantity,
                    alertLevel,
                    product.getPrice(),
                    product.getUnit()
            );
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    }

    private Long getUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            // Mock userId, thực tế cần implement đúng
            return 4L;
        }
        return 4L;
    }
}
