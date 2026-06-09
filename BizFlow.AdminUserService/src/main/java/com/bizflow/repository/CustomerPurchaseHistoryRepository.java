package com.bizflow.repository;

import com.bizflow.entity.CustomerPurchaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerPurchaseHistoryRepository extends JpaRepository<CustomerPurchaseHistory, Long> {
    
    /**
     * Find all purchases for a specific customer with pagination
     */
    Page<CustomerPurchaseHistory> findByCustomerIdOrderByOrderCreatedAtDesc(Long customerId, Pageable pageable);
    
    /**
     * Find all purchases for a specific customer
     */
    List<CustomerPurchaseHistory> findByCustomerIdOrderByOrderCreatedAtDesc(Long customerId);
    
    /**
     * Find purchases within a date range for a customer
     */
    List<CustomerPurchaseHistory> findByCustomerIdAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc(
        Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find by order ID (unique)
     */
    CustomerPurchaseHistory findByOrderId(Long orderId);
    
    /**
     * Count purchases for a customer
     */
    long countByCustomerId(Long customerId);
}
