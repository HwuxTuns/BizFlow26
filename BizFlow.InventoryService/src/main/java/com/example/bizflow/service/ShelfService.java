package com.example.bizflow.service;

import com.example.bizflow.entity.InventoryStock;
import com.example.bizflow.entity.Shelf;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.repository.InventoryStockRepository;
import com.example.bizflow.repository.ShelfRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final CatalogClient catalogClient;

    private static final int WARNING_THRESHOLD = 10;
    private static final int DANGER_THRESHOLD = 0;

    public ShelfService(ShelfRepository shelfRepository,
                        InventoryStockRepository inventoryStockRepository,
                        CatalogClient catalogClient) {
        this.shelfRepository = shelfRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.catalogClient = catalogClient;
    }

    /**
     * OWNER đưa sản phẩm từ kho lên kệ
     */
    @Transactional
    public void moveToShelf(Long productId, int quantity, Long userId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        // Kiểm tra sản phẩm tồn tại
        CatalogClient.ProductSnapshot product = catalogClient.getProduct(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }

        // Kiểm tra tồn kho
        InventoryStock stock = inventoryStockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not in inventory"));

        Integer currentStock = stock.getStock();
        int available = currentStock != null ? currentStock : 0;
        
        if (available < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Insufficient stock in inventory. Available: " + available);
        }

        // Trừ từ kho
        int newInventoryStock = available - quantity;
        stock.setStock(newInventoryStock);
        stock.setUpdatedBy(userId);
        inventoryStockRepository.save(stock);

        // Thêm lên kệ
        Shelf shelf = shelfRepository.findByProductId(productId).orElseGet(() -> {
            Shelf newShelf = new Shelf();
            newShelf.setProductId(productId);
            newShelf.setQuantity(0);
            return newShelf;
        });

        Integer shelfQuantity = shelf.getQuantity();
        int currentShelfQty = shelfQuantity != null ? shelfQuantity : 0;
        int newShelfQty = currentShelfQty + quantity;
        shelf.setQuantity(newShelfQty);
        shelf.setUpdatedBy(userId);
        shelfRepository.save(shelf);

        catalogClient.updateProductStock(productId, newInventoryStock + newShelfQty);
    }

    /**
     * OWNER bỏ sản phẩm khỏi kệ (trả về kho)
     */
    @Transactional
    public void removeFromShelf(Long productId, int quantity, Long userId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        Shelf shelf = shelfRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not on shelf"));

        Integer shelfQuantity = shelf.getQuantity();
        int currentShelfQty = shelfQuantity != null ? shelfQuantity : 0;

        if (currentShelfQty < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Insufficient quantity on shelf. Available: " + currentShelfQty);
        }

        // Trừ khỏi kệ
        int newShelfQty = currentShelfQty - quantity;
        if (newShelfQty == 0) {
            // Tự động xóa nếu = 0
            shelfRepository.delete(shelf);
        } else {
            shelf.setQuantity(newShelfQty);
            shelf.setUpdatedBy(userId);
            shelfRepository.save(shelf);
        }

        // Trả về kho
        InventoryStock stock = inventoryStockRepository.findByProductId(productId).orElseGet(() -> {
            InventoryStock newStock = new InventoryStock();
            newStock.setProductId(productId);
            newStock.setStock(0);
            return newStock;
        });

        Integer stockQuantity = stock.getStock();
        int currentStock = stockQuantity != null ? stockQuantity : 0;
        int newInventoryStock = currentStock + quantity;
        stock.setStock(newInventoryStock);
        stock.setUpdatedBy(userId);
        inventoryStockRepository.save(stock);

        int finalShelfQty = newShelfQty;
        catalogClient.updateProductStock(productId, newInventoryStock + finalShelfQty);
    }

    /**
     * Lấy số lượng sản phẩm trên kệ
     */
    public int getShelfQuantity(Long productId) {
        if (productId == null) {
            return 0;
        }
        return shelfRepository.findByProductId(productId)
                .map(Shelf::getQuantity)
                .orElse(0);
    }

    /**
     * Lấy tất cả sản phẩm trên kệ
     */
    public List<Shelf> getAllShelves() {
        return shelfRepository.findAllByOrderByUpdatedAtDesc();
    }

    /**
     * Lấy sản phẩm cảnh báo (quantity < threshold)
     */
    public List<Shelf> getLowStockShelves(int threshold) {
        return shelfRepository.findByQuantityLessThan(threshold);
    }

    /**
     * Trừ kệ khi bán hàng (dùng bởi SalesService)
     */
    @Transactional
    public void deductFromShelf(Long productId, int quantity, Long orderId, Long userId) {
        if (productId == null || quantity <= 0) {
            return;
        }

        Shelf shelf = shelfRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Product not available on shelf for sale"));

        Integer shelfQuantity = shelf.getQuantity();
        int currentShelfQty = shelfQuantity != null ? shelfQuantity : 0;

        if (currentShelfQty < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Insufficient quantity on shelf. Available: " + currentShelfQty);
        }

        int newQty = currentShelfQty - quantity;
        if (newQty == 0) {
            shelfRepository.delete(shelf);
        } else {
            shelf.setQuantity(newQty);
            shelf.setUpdatedBy(userId);
            shelfRepository.save(shelf);
        }

        int inventoryQty = inventoryStockRepository.findByProductId(productId)
                .map(InventoryStock::getStock)
                .map(q -> q == null ? 0 : q)
                .orElse(0);
        int shelfQty = newQty;
        catalogClient.updateProductStock(productId, inventoryQty + shelfQty);
    }

    /**
     * Cộng kệ khi trả/đổi hàng (dùng bởi SalesService)
     */
    @Transactional
    public void addToShelf(Long productId, int quantity, Long orderId, Long userId) {
        if (productId == null || quantity <= 0) {
            return;
        }

        Shelf shelf = shelfRepository.findByProductId(productId).orElseGet(() -> {
            Shelf created = new Shelf();
            created.setProductId(productId);
            created.setQuantity(0);
            return created;
        });

        Integer shelfQuantity = shelf.getQuantity();
        int currentShelfQty = shelfQuantity != null ? shelfQuantity : 0;
        int newQty = currentShelfQty + quantity;
        shelf.setQuantity(newQty);
        shelf.setUpdatedBy(userId);
        shelfRepository.save(shelf);

        int inventoryQty = inventoryStockRepository.findByProductId(productId)
                .map(InventoryStock::getStock)
                .map(q -> q == null ? 0 : q)
                .orElse(0);
        catalogClient.updateProductStock(productId, inventoryQty + newQty);
    }

    /**
     * Xác định alert level
     */
    public String getAlertLevel(int quantity) {
        if (quantity <= DANGER_THRESHOLD) {
            return "DANGER";
        } else if (quantity < WARNING_THRESHOLD) {
            return "WARNING";
        } else {
            return "NORMAL";
        }
    }
}
