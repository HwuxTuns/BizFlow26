package com.bizflow.service;

import com.bizflow.entity.CustomerPurchaseHistory;
import com.bizflow.repository.CustomerPurchaseHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerPurchaseHistoryService {
    
    private final CustomerPurchaseHistoryRepository repository;
    
    public CustomerPurchaseHistoryService(CustomerPurchaseHistoryRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get purchase history for a customer with pagination
     */
    public Page<CustomerPurchaseHistory> getCustomerPurchaseHistory(Long customerId, Pageable pageable) {
        return repository.findByCustomerIdOrderByOrderCreatedAtDesc(customerId, pageable);
    }
    
    /**
     * Get all purchase history for a customer
     */
    public List<CustomerPurchaseHistory> getCustomerAllPurchases(Long customerId) {
        return repository.findByCustomerIdOrderByOrderCreatedAtDesc(customerId);
    }
    
    /**
     * Get purchase history within a date range
     */
    public List<CustomerPurchaseHistory> getCustomerPurchasesBetweenDates(
            Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByCustomerIdAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc(
            customerId, startDate, endDate);
    }
    
    /**
     * Get total purchase count for a customer
     */
    public long getCustomerPurchaseCount(Long customerId) {
        return repository.countByCustomerId(customerId);
    }
    
    /**
     * Get specific purchase by ID
     */
    public CustomerPurchaseHistory getPurchaseById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
