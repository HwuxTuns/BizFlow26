package com.example.bizflow.dto;

import com.example.bizflow.entity.ProductCostHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCostHistoryDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private BigDecimal costPrice;
    private Integer quantity;
    private BigDecimal totalCost;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;

    public ProductCostHistoryDTO() {
    }

    public static ProductCostHistoryDTO fromEntity(ProductCostHistory entity) {
        ProductCostHistoryDTO dto = new ProductCostHistoryDTO();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setCostPrice(entity.getCostPrice());
        dto.setQuantity(entity.getQuantity());
        dto.setNote(entity.getNote());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCostPrice() != null && entity.getQuantity() != null) {
            dto.setTotalCost(entity.getCostPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        }

        if (entity.getProduct() != null) {
            dto.setProductName(entity.getProduct().getName());
            dto.setProductCode(entity.getProduct().getCode());
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
