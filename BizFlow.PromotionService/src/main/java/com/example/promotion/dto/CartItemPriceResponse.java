package com.example.promotion.dto;

import java.math.BigDecimal;

public class CartItemPriceResponse {
    private Long productId;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private BigDecimal discount;
    private String promoCode;
    private String promoName;
    private String promoType;
    private Integer quantity;

    // Constructors
    public CartItemPriceResponse() {}

    public CartItemPriceResponse(Long productId, BigDecimal basePrice, BigDecimal finalPrice, 
                                 BigDecimal discount, String promoCode, String promoName, 
                                 String promoType, Integer quantity) {
        this.productId = productId;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.discount = discount;
        this.promoCode = promoCode;
        this.promoName = promoName;
        this.promoType = promoType;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = promoType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
