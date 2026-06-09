package com.bizflow.adminproductservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bizflow.adminproductservice.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

		@Query("""
						SELECT p FROM Product p
						WHERE (:active IS NULL OR p.active = :active)
							AND (
									 :q IS NULL OR :q = ''
									 OR (p.sku IS NOT NULL AND LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')))
									 OR (p.productName IS NOT NULL AND LOWER(p.productName) LIKE LOWER(CONCAT('%', :q, '%')))
									 OR (p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))
							)
						""")
		Page<Product> searchProducts(@Param("q") String q, @Param("active") Boolean active, Pageable pageable);
}
