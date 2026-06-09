package com.example.bizflow.service;

import com.example.bizflow.dto.InventoryAlertDTO;
import com.example.bizflow.entity.InventoryStock;
import com.example.bizflow.entity.Shelf;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.repository.InventoryStockRepository;
import com.example.bizflow.repository.ShelfRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryAlertService {

    private final ShelfRepository shelfRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final CatalogClient catalogClient;

    // Ngưỡng cho KỆ
    private static final int SHELF_WARNING_THRESHOLD = 10;
    private static final int SHELF_DANGER_THRESHOLD = 5;

    // Ngưỡng cho KHO
    private static final int WAREHOUSE_WARNING_THRESHOLD = 20;
    private static final int WAREHOUSE_DANGER_THRESHOLD = 10;

    public InventoryAlertService(ShelfRepository shelfRepository,
                                  InventoryStockRepository inventoryStockRepository,
                                  CatalogClient catalogClient) {
        this.shelfRepository = shelfRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.catalogClient = catalogClient;
    }

    /**
     * Lấy tất cả thông báo (kệ + kho)
     * Kệ: kiểm tra realtime
     * Kho: kiểm tra theo lastCheckedDate (giả lập check vào 7h mỗi ngày)
     */
    public List<InventoryAlertDTO> getAllAlerts(LocalDate lastCheckedDate) {
        List<InventoryAlertDTO> alerts = new ArrayList<>();

        // 1. Thông báo từ KỆ (realtime)
        alerts.addAll(getShelfAlerts());

        // 2. Thông báo từ KHO (chỉ hiển thị nếu lastCheckedDate < today)
        LocalDate today = LocalDate.now();
        if (lastCheckedDate == null || lastCheckedDate.isBefore(today)) {
            alerts.addAll(getWarehouseAlerts());
        }

        return alerts;
    }

    /**
     * Lấy thông báo từ KỆ (shelves)
     * - quantity < 10: WARNING
     * - quantity < 5: DANGER
     */
    public List<InventoryAlertDTO> getShelfAlerts() {
        List<InventoryAlertDTO> alerts = new ArrayList<>();

        try {
            List<Shelf> allShelves = shelfRepository.findAll();

            for (Shelf shelf : allShelves) {
                if (shelf.getProductId() == null) {
                    continue;
                }

                Integer quantity = shelf.getQuantity();
                if (quantity == null) {
                    quantity = 0;
                }

                // Chỉ thông báo nếu dưới ngưỡng WARNING
                if (quantity < SHELF_WARNING_THRESHOLD) {
                    CatalogClient.ProductSnapshot product = catalogClient.getProduct(shelf.getProductId());
                    if (product == null) {
                        continue;
                    }

                    String level = quantity < SHELF_DANGER_THRESHOLD ? "DANGER" : "WARNING";
                    String message = String.format(
                        "Sản phẩm '%s' trên kệ chỉ còn %d %s",
                        product.getName(),
                        quantity,
                        product.getUnit() != null ? product.getUnit() : "đơn vị"
                    );

                    InventoryAlertDTO alert = new InventoryAlertDTO(
                        "SHELF",
                        level,
                        shelf.getProductId(),
                        product.getName(),
                        product.getCode(),
                        quantity,
                        message
                    );

                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            // Fail-safe: return empty list
        }

        return alerts;
    }

    /**
     * Lấy thông báo từ KHO (inventory_stocks)
     * - quantity < 20: WARNING
     * - quantity < 10: DANGER
     */
    public List<InventoryAlertDTO> getWarehouseAlerts() {
        List<InventoryAlertDTO> alerts = new ArrayList<>();

        try {
            List<InventoryStock> allStocks = inventoryStockRepository.findAll();

            for (InventoryStock stock : allStocks) {
                if (stock.getProductId() == null) {
                    continue;
                }

                Integer quantity = stock.getStock();
                if (quantity == null) {
                    quantity = 0;
                }

                // Chỉ thông báo nếu dưới ngưỡng WARNING
                if (quantity < WAREHOUSE_WARNING_THRESHOLD) {
                    CatalogClient.ProductSnapshot product = catalogClient.getProduct(stock.getProductId());
                    if (product == null) {
                        continue;
                    }

                    String level = quantity < WAREHOUSE_DANGER_THRESHOLD ? "DANGER" : "WARNING";
                    String message = String.format(
                        "Sản phẩm '%s' trong kho chỉ còn %d %s",
                        product.getName(),
                        quantity,
                        product.getUnit() != null ? product.getUnit() : "đơn vị"
                    );

                    InventoryAlertDTO alert = new InventoryAlertDTO(
                        "WAREHOUSE",
                        level,
                        stock.getProductId(),
                        product.getName(),
                        product.getCode(),
                        quantity,
                        message
                    );

                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            // Fail-safe: return empty list
        }

        return alerts;
    }
}
