package com.example.promotion.repository;

import com.example.promotion.entity.PromotionTarget;
import com.example.promotion.entity.PromotionTarget.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, Long> {

    List<PromotionTarget> findByPromotion_Id(Long promotionId);

    List<PromotionTarget> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}
