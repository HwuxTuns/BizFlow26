package com.example.promotion.dto;

import com.example.promotion.entity.PromotionTarget.TargetType;

public class PromotionTargetDTO {

    private Long id;
    private TargetType targetType;
    private Long targetId;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
}
