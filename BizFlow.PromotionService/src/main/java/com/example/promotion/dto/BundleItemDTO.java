package com.example.promotion.dto;

public class BundleItemDTO {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Long mainProductId;
    private Integer mainQuantity;
    private Long giftProductId;
    private Integer giftQuantity;
    private String giftDiscountType;
    private Double giftDiscountValue;
    private String status;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getMainProductId() { return mainProductId; }
    public void setMainProductId(Long mainProductId) { this.mainProductId = mainProductId; }

    public Integer getMainQuantity() { return mainQuantity; }
    public void setMainQuantity(Integer mainQuantity) { this.mainQuantity = mainQuantity; }

    public Long getGiftProductId() { return giftProductId; }
    public void setGiftProductId(Long giftProductId) { this.giftProductId = giftProductId; }

    public Integer getGiftQuantity() { return giftQuantity; }
    public void setGiftQuantity(Integer giftQuantity) { this.giftQuantity = giftQuantity; }

    public String getGiftDiscountType() { return giftDiscountType; }
    public void setGiftDiscountType(String giftDiscountType) { this.giftDiscountType = giftDiscountType; }

    public Double getGiftDiscountValue() { return giftDiscountValue; }
    public void setGiftDiscountValue(Double giftDiscountValue) { this.giftDiscountValue = giftDiscountValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
