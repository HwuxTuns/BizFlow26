package com.example.bizflow.repository;

import com.example.bizflow.entity.ProductCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCostRepository extends JpaRepository<ProductCost, Long> {

    Optional<ProductCost> findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
