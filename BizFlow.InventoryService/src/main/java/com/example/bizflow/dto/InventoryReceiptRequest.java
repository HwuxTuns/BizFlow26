package com.example.bizflow.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryReceiptRequest {

    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String note;
    private Long userId;

    // ===== GETTERS =====
    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getNote() {
        return note;
    }

    public Long getUserId() {
        return userId;
    }

    // ===== SETTERS =====
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
