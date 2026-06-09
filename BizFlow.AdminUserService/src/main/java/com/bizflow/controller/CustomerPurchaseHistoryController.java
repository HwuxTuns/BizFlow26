package com.bizflow.controller;

import com.bizflow.entity.CustomerPurchaseHistory;
import com.bizflow.service.CustomerPurchaseHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for customer purchase history
 * Provides access to customer order information received via Kafka
 */
@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerPurchaseHistoryController {
    
    private final CustomerPurchaseHistoryService purchaseHistoryService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public CustomerPurchaseHistoryController(CustomerPurchaseHistoryService purchaseHistoryService) {
        this.purchaseHistoryService = purchaseHistoryService;
    }
    
    /**
     * Get paginated purchase history for a customer
     * GET /api/admin/customers/{customerId}/purchase-history?page=0&size=10
     */
    @GetMapping("/{customerId}/purchase-history")
    public ResponseEntity<?> getPurchaseHistory(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerPurchaseHistory> history = 
                purchaseHistoryService.getCustomerPurchaseHistory(customerId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history.getContent());
            response.put("totalElements", history.getTotalElements());
            response.put("totalPages", history.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Get all purchases for a customer (no pagination)
     * GET /api/admin/customers/{customerId}/purchase-history/all
     */
    @GetMapping("/{customerId}/purchase-history/all")
    public ResponseEntity<?> getAllPurchaseHistory(@PathVariable Long customerId) {
        try {
            List<CustomerPurchaseHistory> history = 
                purchaseHistoryService.getCustomerAllPurchases(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("totalCount", history.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Get purchases within a date range
     * GET /api/admin/customers/{customerId}/purchase-history/date-range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/{customerId}/purchase-history/date-range")
    public ResponseEntity<?> getPurchasesByDateRange(
            @PathVariable Long customerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate, dateFormatter);
            LocalDateTime end = LocalDateTime.parse(endDate, dateFormatter);
            
            List<CustomerPurchaseHistory> history = 
                purchaseHistoryService.getCustomerPurchasesBetweenDates(customerId, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("totalCount", history.size());
            response.put("dateRange", Map.of("start", startDate, "end", endDate));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Get purchase count for a customer
     * GET /api/admin/customers/{customerId}/purchase-history/count
     */
    @GetMapping("/{customerId}/purchase-history/count")
    public ResponseEntity<?> getPurchaseCount(@PathVariable Long customerId) {
        try {
            long count = purchaseHistoryService.getCustomerPurchaseCount(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customerId", customerId);
            response.put("purchaseCount", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Get specific purchase details
     * GET /api/admin/customers/{customerId}/purchase-history/{id}
     */
    @GetMapping("/{customerId}/purchase-history/{id}")
    public ResponseEntity<?> getPurchaseDetail(
            @PathVariable Long customerId,
            @PathVariable Long id) {
        
        try {
            CustomerPurchaseHistory history = purchaseHistoryService.getPurchaseById(id);
            
            if (history == null || !history.getCustomerId().equals(customerId)) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage()));
        }
    }
}
