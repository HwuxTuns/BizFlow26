package com.example.bizflow.dto;

public class InventoryAlertDTO {
    private String type;          // "SHELF" hoặc "WAREHOUSE"
    private String level;         // "WARNING" hoặc "DANGER"
    private Long productId;
    private String productName;
    private String productCode;
    private Integer quantity;
    private String message;

    public InventoryAlertDTO() {
    }

    public InventoryAlertDTO(String type, String level, Long productId, String productName, 
                           String productCode, Integer quantity, String message) {
        this.type = type;
        this.level = level;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.message = message;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
