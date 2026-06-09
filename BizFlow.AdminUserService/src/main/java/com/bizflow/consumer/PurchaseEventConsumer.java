package com.bizflow.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.bizflow.entity.CustomerPurchaseHistory;
import com.bizflow.event.PurchaseEvent;
import com.bizflow.repository.CustomerPurchaseHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kafka consumer for customer purchase events
 * Listens on topic: customer-purchases
 */
@Service
public class PurchaseEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseEventConsumer.class);
    
    private final CustomerPurchaseHistoryRepository purchaseHistoryRepository;
    private final ObjectMapper objectMapper;
    
    public PurchaseEventConsumer(CustomerPurchaseHistoryRepository purchaseHistoryRepository,
                                ObjectMapper objectMapper) {
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Listen and process purchase events from Kafka
     * @param event The purchase event received from order service
     */
    @KafkaListener(topics = "customer-purchases", groupId = "admin-user-service")
    public void consumePurchaseEvent(PurchaseEvent event) {
        try {
            logger.info("Received purchase event for customer {} with order {}", 
                event.getCustomerId(), event.getOrderId());
            
            if (event.getOrderId() != null && event.getCustomerId() != null) {
                // Check if order already exists
                CustomerPurchaseHistory existing = purchaseHistoryRepository.findByOrderId(event.getOrderId());
                
                CustomerPurchaseHistory history = existing != null ? existing : new CustomerPurchaseHistory();
                
                // Map event to entity
                history.setOrderId(event.getOrderId());
                history.setCustomerId(event.getCustomerId());
                history.setInvoiceNumber(event.getInvoiceNumber());
                history.setTotalAmount(event.getTotalAmount());
                history.setStatus(event.getStatus());
                history.setOrderCreatedAt(event.getCreatedAt());
                history.setPaymentMethod(event.getPaymentMethod());
                
                // Convert order items to JSON
                if (event.getOrderItems() != null && !event.getOrderItems().isEmpty()) {
                    try {
                        String itemsJson = objectMapper.writeValueAsString(event.getOrderItems());
                        history.setOrderItemsJson(itemsJson);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        logger.warn("Failed to serialize order items: {}", e.getMessage());
                    }
                }
                
                // Save to database
                purchaseHistoryRepository.save(history);
                logger.info("Saved purchase history for customer {} with order {}", 
                    event.getCustomerId(), event.getOrderId());
                
            } else {
                logger.warn("Skipping event - missing orderId or customerId: {}", event);
            }
        } catch (RuntimeException e) {
            logger.error("Error processing purchase event: {}", e.getMessage(), e);
        }
    }
}
