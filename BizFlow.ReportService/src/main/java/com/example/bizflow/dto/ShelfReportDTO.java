package com.example.bizflow.dto;

public class ShelfReportDTO {
    private Long productId;
    private String productName;
    private String productCode;
    private Long categoryId;
    private Integer quantity;
    private String alertLevel;

    public ShelfReportDTO() {
    }

    public ShelfReportDTO(Long productId, String productName, String productCode,
                          Long categoryId, Integer quantity, String alertLevel) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.alertLevel = alertLevel;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }
}
