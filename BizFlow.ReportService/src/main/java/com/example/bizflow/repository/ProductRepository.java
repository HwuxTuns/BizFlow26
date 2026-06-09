package com.example.bizflow.repository;

import com.example.bizflow.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Count all products
    @Override
    long count();

    // Count active products
    long countByActive(Boolean active);
}
