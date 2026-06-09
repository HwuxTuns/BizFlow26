package com.example.bizflow.repository;

import com.example.bizflow.entity.ProductCostHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductCostHistoryRepository extends JpaRepository<ProductCostHistory, Long> {

    List<ProductCostHistory> findByProductIdOrderByCreatedAtDesc(Long productId);

    Page<ProductCostHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    @Query("SELECT h FROM ProductCostHistory h WHERE h.productId = :productId "
            + "AND h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt DESC")
    List<ProductCostHistory> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT h FROM ProductCostHistory h WHERE h.createdAt BETWEEN :startDate AND :endDate "
            + "ORDER BY h.createdAt DESC")
    List<ProductCostHistory> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT h FROM ProductCostHistory h LEFT JOIN FETCH h.product ORDER BY h.createdAt DESC")
    List<ProductCostHistory> findAllWithProduct();
}
