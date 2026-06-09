package com.example.promotion.dto;

import com.example.promotion.entity.Promotion.DiscountType;

import java.time.LocalDateTime;
import java.util.List;

public class PromotionDTO {

    private Long id;
    private String code;
    private String name;
    private String description;

    private DiscountType discountType;
    private Double discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active;
    private Integer maxQuantity;
    private Integer usedQuantity;
    private Integer remainingQuantity;
    private Boolean isExhausted;

    private List<PromotionTargetDTO> targets;
    private List<BundleItemDTO> bundleItems;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(Integer maxQuantity) { this.maxQuantity = maxQuantity; }

    public Integer getUsedQuantity() { return usedQuantity; }
    public void setUsedQuantity(Integer usedQuantity) { this.usedQuantity = usedQuantity; }

    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public Boolean getIsExhausted() { return isExhausted; }
    public void setIsExhausted(Boolean isExhausted) { this.isExhausted = isExhausted; }

    public List<PromotionTargetDTO> getTargets() { return targets; }
    public void setTargets(List<PromotionTargetDTO> targets) { this.targets = targets; }

    public List<BundleItemDTO> getBundleItems() { return bundleItems; }
    public void setBundleItems(List<BundleItemDTO> bundleItems) { this.bundleItems = bundleItems; }
}
