package com.example.bizflow.dto;

public class LowStockAlertDTO {

    private Long productId;
    private String productName;
    private String productCode;
    private Long categoryId;
    private Integer currentStock;
    private Integer minStockThreshold;
    private String alertLevel; // "CRITICAL", "WARNING", "LOW"
    private String lastRestockDate;

    public LowStockAlertDTO() {
    }

    public LowStockAlertDTO(Long productId, String productName, String productCode,
            Long categoryId, Integer currentStock, Integer minStockThreshold,
            String alertLevel) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.categoryId = categoryId;
        this.currentStock = currentStock;
        this.minStockThreshold = minStockThreshold;
        this.alertLevel = alertLevel;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(Integer minStockThreshold) {
        this.minStockThreshold = minStockThreshold;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getLastRestockDate() {
        return lastRestockDate;
    }

    public void setLastRestockDate(String lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }
}
