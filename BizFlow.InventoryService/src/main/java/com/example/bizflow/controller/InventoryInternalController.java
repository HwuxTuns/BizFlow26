package com.example.bizflow.controller;

import com.example.bizflow.dto.InventoryReceiptRequest;
import com.example.bizflow.dto.InventoryReceiptResponse;
import com.example.bizflow.entity.InventoryStock;
import com.example.bizflow.entity.Shelf;
import com.example.bizflow.repository.InventoryStockRepository;
import com.example.bizflow.repository.ShelfRepository;
import com.example.bizflow.service.InventoryService;
import com.example.bizflow.service.ShelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/inventory")
public class InventoryInternalController {

    private final InventoryService inventoryService;
    private final InventoryStockRepository inventoryStockRepository;
    private final ShelfService shelfService;
    private final ShelfRepository shelfRepository;

    public InventoryInternalController(InventoryService inventoryService,
                                       InventoryStockRepository inventoryStockRepository,
                                       ShelfService shelfService,
                                       ShelfRepository shelfRepository) {
        this.inventoryService = inventoryService;
        this.inventoryStockRepository = inventoryStockRepository;
        this.shelfService = shelfService;
        this.shelfRepository = shelfRepository;
    }

    @PostMapping("/sales")
    public ResponseEntity<Void> applySale(@RequestBody SaleRequest request) {
        if (request == null || request.items == null || request.items.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // Trừ từ shelf thay vì inventory_stock
        for (SaleItem item : request.items) {
            if (item.productId == null || item.quantity == null) {
                continue;
            }
            if (item.quantity > 0) {
                shelfService.deductFromShelf(item.productId, item.quantity, request.orderId, request.userId);
            } else if (item.quantity < 0) {
                shelfService.addToShelf(item.productId, Math.abs(item.quantity), request.orderId, request.userId);
            }
        }
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/shelves/stocks")
    public ResponseEntity<List<StockItem>> getShelfStocks(@RequestBody List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<StockItem> result = new ArrayList<>();
        for (Long productId : productIds) {
            int stock = shelfRepository.findByProductId(productId)
                    .map(Shelf::getQuantity)
                    .orElse(0);
            StockItem item = new StockItem();
            item.productId = productId;
            item.stock = stock;
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stocks")
    public ResponseEntity<List<StockItem>> getStocks(@RequestBody List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<InventoryStock> stocks = inventoryStockRepository.findByProductIdIn(productIds);
        Map<Long, Integer> stockMap = new HashMap<>();
        for (InventoryStock stock : stocks) {
            stockMap.put(stock.getProductId(), stock.getStock() == null ? 0 : stock.getStock());
        }
        List<StockItem> result = new ArrayList<>();
        for (Long productId : productIds) {
            StockItem item = new StockItem();
            item.productId = productId;
            item.stock = stockMap.getOrDefault(productId, 0);
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockItem>> getLowStock(
            @RequestParam(required = false, defaultValue = "10") int threshold) {
        List<LowStockItem> result = inventoryStockRepository.findLowStock(threshold).stream()
                .map(view -> new LowStockItem(view.getProductId(), view.getStock()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/receipts")
    public ResponseEntity<InventoryReceiptResponse> receiveStock(@RequestBody InventoryReceiptRequest request) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().build();
        }

        var tx = inventoryService.receiveStock(
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
                tx == null ? null : tx.getId()
        ));
    }

    private List<InventoryService.SaleItem> toServiceItems(List<SaleItem> items) {
        List<InventoryService.SaleItem> result = new ArrayList<>();
        for (SaleItem item : items) {
            InventoryService.SaleItem saleItem = new InventoryService.SaleItem();
            saleItem.setProductId(item.productId);
            saleItem.setQuantity(item.quantity);
            saleItem.setUnitPrice(item.unitPrice);
            result.add(saleItem);
        }
        return result;
    }

    private static class SaleRequest {
        public Long orderId;
        public Long userId;
        public List<SaleItem> items;
    }

    private static class SaleItem {
        public Long productId;
        public Integer quantity;
        public java.math.BigDecimal unitPrice;
    }

    public static class StockItem {
        public Long productId;
        public Integer stock;
    }

    public static class LowStockItem {
        public Long productId;
        public Integer stock;

        public LowStockItem(Long productId, Integer stock) {
            this.productId = productId;
            this.stock = stock;
        }
    }
}
