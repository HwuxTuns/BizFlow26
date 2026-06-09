package com.example.promotion.repository;

import com.example.promotion.entity.Promotion;
import com.example.promotion.entity.PromotionTarget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    Optional<Promotion> findByIdForUpdate(@Param("id") Long id);

    Optional<Promotion> findByCode(String code);

    List<Promotion> findByActiveTrue();

    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
          AND (p.startDate IS NULL OR p.startDate <= :now)
          AND (p.endDate IS NULL OR p.endDate >= :now)
    """)
    List<Promotion> findActivePromotions(LocalDateTime now);

    @Query("""
        SELECT p FROM Promotion p
        WHERE p.code = :code
          AND p.active = true
          AND (p.startDate IS NULL OR p.startDate <= :now)
          AND (p.endDate IS NULL OR p.endDate >= :now)
    """)
    Optional<Promotion> findActivePromotionByCode(String code, LocalDateTime now);

    @Query("""
        SELECT DISTINCT p FROM Promotion p
        LEFT JOIN p.targets t
        WHERE (:q IS NULL OR :q = '' OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:discountType IS NULL OR p.discountType = :discountType)
          AND (:active IS NULL OR p.active = :active)
          AND (:targetType IS NULL OR t.targetType = :targetType)
          AND (:targetId IS NULL OR t.targetId = :targetId)
    """)
    List<Promotion> searchPromotions(
            @Param("q") String q,
            @Param("discountType") Promotion.DiscountType discountType,
            @Param("active") Boolean active,
            @Param("targetType") PromotionTarget.TargetType targetType,
            @Param("targetId") Long targetId
    );

    @Query("""
        SELECT p.code FROM Promotion p
        WHERE LOWER(p.code) LIKE LOWER(CONCAT(:prefix, '-%'))
    """)
    List<String> findCodesByPrefix(@Param("prefix") String prefix);
}
