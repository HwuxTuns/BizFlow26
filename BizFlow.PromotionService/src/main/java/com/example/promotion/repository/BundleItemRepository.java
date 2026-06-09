package com.example.promotion.repository;

import com.example.promotion.entity.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {

    List<BundleItem> findByPromotion_Id(Long promotionId);
}
