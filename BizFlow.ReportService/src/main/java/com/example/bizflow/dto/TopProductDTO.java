package com.example.bizflow.dto;

import java.math.BigDecimal;

public class TopProductDTO {

    private Long productId;
    private String productName;
    private String productCode;
    private Long categoryId;
    private long quantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal profit;
    private Integer currentStock;

    public TopProductDTO() {
    }

    public TopProductDTO(Long productId, String productName, String productCode, Long categoryId,
            long quantitySold, BigDecimal totalRevenue, BigDecimal totalCost,
            BigDecimal profit, Integer currentStock) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.categoryId = categoryId;
        this.quantitySold = quantitySold;
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
        this.profit = profit;
        this.currentStock = currentStock;
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

    public long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }
}
