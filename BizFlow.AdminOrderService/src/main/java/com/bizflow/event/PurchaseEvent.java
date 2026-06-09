package com.bizflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka event for customer purchases
 * Sent to topic: customer-purchases
 */
public class PurchaseEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long customerId;
    private Long userId;
    private String invoiceNumber;
    private java.math.BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItems;
    private String paymentMethod;
    private Long paymentId;

    public PurchaseEvent() {}

    public PurchaseEvent(Long orderId, Long customerId, Long userId, String invoiceNumber,
                         java.math.BigDecimal totalAmount, String status, LocalDateTime createdAt,
                         List<OrderItemDTO> orderItems, String paymentMethod, Long paymentId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.userId = userId;
        this.invoiceNumber = invoiceNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.orderItems = orderItems;
        this.paymentMethod = paymentMethod;
        this.paymentId = paymentId;
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    @Override
    public String toString() {
        return "PurchaseEvent{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", userId=" + userId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }

    // Nested class for order items
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private Long productId;
        private String productName;
        private java.math.BigDecimal price;
        private Integer quantity;

        public OrderItemDTO() {}

        public OrderItemDTO(Long id, Long productId, String productName,
                           java.math.BigDecimal price, Integer quantity) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        @Override
        public String toString() {
            return "OrderItemDTO{" +
                    "productId=" + productId +
                    ", productName='" + productName + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
