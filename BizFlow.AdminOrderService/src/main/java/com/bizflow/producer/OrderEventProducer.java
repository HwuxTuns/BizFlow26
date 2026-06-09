package com.bizflow.producer;

import com.bizflow.event.PurchaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for publishing customer purchase events
 * Publishes to topic: customer-purchases
 */
@Service
public class OrderEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "customer-purchases";
    
    private final KafkaTemplate<String, PurchaseEvent> kafkaTemplate;
    
    public OrderEventProducer(KafkaTemplate<String, PurchaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publish purchase event to Kafka
     * @param event The purchase event to publish
     */
    public void publishPurchaseEvent(PurchaseEvent event) {
        try {
            if (event.getCustomerId() != null) {
                // Use customer_id as message key for partition routing
                String key = event.getCustomerId().toString();
                kafkaTemplate.send(TOPIC, key, event);
                logger.info("Published purchase event for customer {} with order {}", 
                    event.getCustomerId(), event.getOrderId());
            } else {
                logger.warn("Skipping publish for order {} - no customer ID", event.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error publishing purchase event for order {}: {}", 
                event.getOrderId(), e.getMessage(), e);
        }
    }
}
