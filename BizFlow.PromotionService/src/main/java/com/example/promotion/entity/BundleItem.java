package com.example.promotion.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "bundle_items",
    indexes = {
        @Index(name = "idx_bundle_product", columnList = "product_id")
    }
)
public class BundleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bundle_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "main_product_id", nullable = false)
    private Long mainProductId;

    @Column(name = "main_quantity", nullable = false)
    private Integer mainQuantity;

    @Column(name = "gift_product_id", nullable = false)
    private Long giftProductId;

    @Column(name = "gift_quantity", nullable = false)
    private Integer giftQuantity;

    @Column(name = "gift_discount_type", nullable = false)
    private String giftDiscountType;

    @Column(name = "gift_discount_value", nullable = false)
    private Double giftDiscountValue;

    @Column(name = "status", nullable = false)
    private String status;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }

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
