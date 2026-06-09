package com.example.bizflow.repository;

import com.example.bizflow.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByProductId(Long productId);
    List<InventoryStock> findByProductIdIn(List<Long> productIds);

    @Query("""
        SELECT i.productId AS productId, i.stock AS stock
        FROM InventoryStock i
        WHERE i.stock <= :threshold
        """)
    List<LowStockView> findLowStock(@Param("threshold") int threshold);

    interface LowStockView {
        Long getProductId();
        Integer getStock();
    }
}
