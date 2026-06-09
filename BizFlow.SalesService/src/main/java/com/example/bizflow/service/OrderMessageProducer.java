package com.example.bizflow.service;

import com.example.bizflow.config.RabbitMQConfig;
import com.example.bizflow.dto.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageProducer.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public OrderMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Gửi message đơn hàng mới vào RabbitMQ
     * Message sẽ được route đến nhiều queue khác nhau:
     * - Order queue: Lưu trữ lịch sử đơn hàng
     * - Notification queue: Gửi thông báo cho khách hàng
     * - Inventory queue: Cập nhật tồn kho
     */
    public void sendOrderCreatedMessage(OrderMessage orderMessage) {
        try {
            // Gửi vào order queue
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                orderMessage
            );
            logger.info("✅ [RabbitMQ] Sent order message to ORDER queue: Order #{}", 
                       orderMessage.getOrderCode());
            
            // Gửi vào notification queue
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                orderMessage
            );
            logger.info("✅ [RabbitMQ] Sent order message to NOTIFICATION queue: Order #{}", 
                       orderMessage.getOrderCode());
            
            // Gửi vào inventory queue
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.INVENTORY_ROUTING_KEY,
                orderMessage
            );
            logger.info("✅ [RabbitMQ] Sent order message to INVENTORY queue: Order #{}", 
                       orderMessage.getOrderCode());
            
        } catch (Exception e) {
            logger.error("❌ [RabbitMQ] Failed to send order message: {}", e.getMessage(), e);
        }
    }
}
