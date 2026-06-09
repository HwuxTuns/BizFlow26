package com.example.promotion.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "promotion_targets",
    indexes = {
        @Index(name = "idx_target_type_id", columnList = "target_type, target_id")
    }
)
public class PromotionTarget {

    public enum TargetType {
        PRODUCT,
        CATEGORY,
        CUSTOMER_GROUP,
        BRANCH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_target_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
}
