package com.example.bizflow.service;

import com.example.bizflow.config.RabbitMQConfig;
import com.example.bizflow.dto.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);
    
    /**
     * Consumer 1: Xử lý lưu trữ lịch sử đơn hàng
     * Lắng nghe message từ ORDER queue
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderQueue(OrderMessage orderMessage) {
        try {
            logger.info("📦 [ORDER CONSUMER] Received order: #{} - Customer: {} - Total: {}",
                       orderMessage.getOrderCode(),
                       orderMessage.getCustomerName(),
                       orderMessage.getTotalAmount());
            
           
            // Ví dụ: Lưu vào bảng order_history, gửi analytics, etc.
            
            logger.info("✅ [ORDER CONSUMER] Successfully processed order #{}", 
                       orderMessage.getOrderCode());
            
        } catch (Exception e) {
            logger.error("❌ [ORDER CONSUMER] Failed to process order: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Consumer 2: Xử lý gửi thông báo
     * Lắng nghe message từ NOTIFICATION queue
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationQueue(OrderMessage orderMessage) {
        try {
            logger.info("🔔 [NOTIFICATION CONSUMER] Processing notification for order: #{}", 
                       orderMessage.getOrderCode());
            
          
            // Ví dụ:
            String message = String.format(
                "Xin chào %s! Đơn hàng #%s của bạn đã được tạo thành công. " +
                "Tổng tiền: %s VNĐ. Cảm ơn bạn đã mua hàng!",
                orderMessage.getCustomerName(),
                orderMessage.getOrderCode(),
                orderMessage.getTotalAmount()
            );
            
            logger.info("📧 [NOTIFICATION CONSUMER] Notification sent to: {} - Phone: {}", 
                       orderMessage.getCustomerName(), 
                       orderMessage.getCustomerPhone());
            logger.info("   Message: {}", message);
            
        } catch (Exception e) {
            logger.error("❌ [NOTIFICATION CONSUMER] Failed to send notification: {}", 
                        e.getMessage(), e);
        }
    }
    
    /**
     * Consumer 3: Xử lý cập nhật tồn kho
     * Lắng nghe message từ INVENTORY queue
     */
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE)
    public void handleInventoryQueue(OrderMessage orderMessage) {
        try {
            logger.info("📊 [INVENTORY CONSUMER] Processing inventory update for order: #{}", 
                       orderMessage.getOrderCode());
            
           
            // Ví dụ: Giảm số lượng trong bảng inventory
            if (orderMessage.getItems() != null) {
                orderMessage.getItems().forEach(item -> {
                    logger.info("   ⤷ Product #{}: {} - Quantity sold: {}", 
                               item.getProductId(), 
                               item.getProductName(), 
                               item.getQuantity());
                    // Thực hiện cập nhật database: 
                    // inventoryService.reduceStock(item.getProductId(), item.getQuantity());
                });
            }
            
            logger.info("✅ [INVENTORY CONSUMER] Successfully updated inventory for order #{}", 
                       orderMessage.getOrderCode());
            
        } catch (Exception e) {
            logger.error("❌ [INVENTORY CONSUMER] Failed to update inventory: {}", 
                        e.getMessage(), e);
        }
    }
}
