package com.example.bizflow.repository;

import com.example.bizflow.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findTop50ByProductIdOrderByCreatedAtDesc(Long productId);
}
