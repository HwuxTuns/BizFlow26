package com.example.bizflow.service;

import com.example.bizflow.entity.InventoryStock;
import com.example.bizflow.entity.InventoryTransaction;
import com.example.bizflow.entity.InventoryTransactionType;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.repository.InventoryStockRepository;
import com.example.bizflow.repository.InventoryTransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final CatalogClient catalogClient;
    private final JdbcTemplate jdbcTemplate;

    public InventoryService(InventoryTransactionRepository inventoryTransactionRepository,
                            InventoryStockRepository inventoryStockRepository,
                            CatalogClient catalogClient,
                            JdbcTemplate jdbcTemplate) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.catalogClient = catalogClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public InventoryTransaction receiveStock(Long productId, int quantity, BigDecimal unitPrice, String note, Long userId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }

        InventoryStock stockRow = inventoryStockRepository.findByProductId(productId).orElseGet(() -> {
            InventoryStock created = new InventoryStock();
            created.setProductId(productId);
            created.setStock(0);
            return created;
        });
        Integer stockValue = stockRow.getStock();
        int current = stockValue != null ? stockValue : 0;
        int updated = current + quantity;
        stockRow.setStock(updated);
        stockRow.setUpdatedBy(userId);
        inventoryStockRepository.save(stockRow);

        syncCatalogTotalStock(productId, updated);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setProductId(productId);
        tx.setTransactionType(InventoryTransactionType.IN);
        tx.setQuantity(quantity);
        tx.setUnitPrice(unitPrice);
        tx.setReferenceType("RECEIPT");
        tx.setNote(note);
        tx.setCreatedBy(userId);
        return inventoryTransactionRepository.save(tx);
    }

    public int getAvailableStock(Long productId) {
        if (productId == null) {
            return 0;
        }
        return inventoryStockRepository.findByProductId(productId)
                .map(InventoryStock::getStock)
                .orElse(0);
    }

    @Transactional
    public void applySale(Long orderId, List<SaleItem> items, Long userId) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (SaleItem item : items) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            Integer qtyValue = item.getQuantity();
            int qty = qtyValue != null ? qtyValue : 0;
            if (qty <= 0) {
                continue;
            }

            InventoryStock stockRow = inventoryStockRepository.findByProductId(item.getProductId())
                    .orElseGet(() -> {
                        InventoryStock created = new InventoryStock();
                        created.setProductId(item.getProductId());
                        created.setStock(0);
                        return created;
                    });
            Integer currentValue = stockRow.getStock();
            int current = currentValue != null ? currentValue : 0;
            if (current < qty) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product: " + item.getProductId()
                );
            }

            int updated = current - qty;
            stockRow.setStock(updated);
            stockRow.setUpdatedBy(userId);
            inventoryStockRepository.save(stockRow);

            syncCatalogTotalStock(item.getProductId(), updated);

            InventoryTransaction tx = new InventoryTransaction();
            tx.setProductId(item.getProductId());
            tx.setTransactionType(InventoryTransactionType.SALE);
            tx.setQuantity(qty);
            tx.setUnitPrice(item.getUnitPrice());
            tx.setReferenceType("ORDER");
            tx.setReferenceId(orderId);
            tx.setCreatedBy(userId);
            inventoryTransactionRepository.save(tx);
        }
    }

    @Transactional
    public InventoryTransaction adjustStock(Long productId, int newQuantity, String reason, String note, Long userId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        if (newQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }

        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }

        InventoryStock stockRow = inventoryStockRepository.findByProductId(productId).orElseGet(() -> {
            InventoryStock created = new InventoryStock();
            created.setProductId(productId);
            created.setStock(0);
            return created;
        });

        Integer currentStockValue = stockRow.getStock();
        int currentStock = currentStockValue != null ? currentStockValue : 0;
        int difference = newQuantity - currentStock;

        if (difference == 0) {
            return null;
        }

        stockRow.setStock(newQuantity);
        stockRow.setUpdatedBy(userId);
        inventoryStockRepository.save(stockRow);

        syncCatalogTotalStock(productId, newQuantity);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setProductId(productId);
        tx.setTransactionType(InventoryTransactionType.ADJUST);
        tx.setQuantity(Math.abs(difference));
        tx.setReferenceType(reason != null ? reason : "AUDIT");
        tx.setNote(buildAdjustNote(currentStock, newQuantity, difference, note));
        tx.setCreatedBy(userId);
        return inventoryTransactionRepository.save(tx);
    }

    @Transactional
    public InventoryTransaction manualStockOut(Long productId, int quantity, String reason, String note, Long userId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }

        InventoryStock stockRow = inventoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock record not found"));

        Integer currentStockValue = stockRow.getStock();
        int currentStock = currentStockValue != null ? currentStockValue : 0;
        if (currentStock < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Current: " + currentStock + ", Requested: " + quantity);
        }

        int newStock = currentStock - quantity;
        stockRow.setStock(newStock);
        stockRow.setUpdatedBy(userId);
        inventoryStockRepository.save(stockRow);

        syncCatalogTotalStock(productId, newStock);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setProductId(productId);
        tx.setTransactionType(InventoryTransactionType.OUT);
        tx.setQuantity(quantity);
        tx.setReferenceType(reason != null ? reason : "MANUAL_OUT");
        tx.setNote(note);
        tx.setCreatedBy(userId);
        return inventoryTransactionRepository.save(tx);
    }

    private String buildAdjustNote(int oldQty, int newQty, int diff, String userNote) {
        String direction = diff > 0 ? "Tang" : "Giam";
        String base = String.format("Dieu chinh kho: %s %d (Cu: %d -> Moi: %d)",
                direction, Math.abs(diff), oldQty, newQty);
        if (userNote != null && !userNote.isBlank()) {
            return base + " | " + userNote;
        }
        return base;
    }

    private void syncCatalogTotalStock(Long productId, int inventoryStockQuantity) {
        if (productId == null || inventoryStockQuantity < 0) {
            return;
        }
        int shelfQuantity = 0;
        try {
            Integer sum = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(quantity), 0) FROM shelves WHERE product_id = ?",
                    Integer.class,
                    productId
            );
            shelfQuantity = sum == null ? 0 : sum;
        } catch (Exception ignored) {
            shelfQuantity = 0;
        }
        int total = inventoryStockQuantity + shelfQuantity;
        catalogClient.updateProductStock(productId, total);
    }

    public static class SaleItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
