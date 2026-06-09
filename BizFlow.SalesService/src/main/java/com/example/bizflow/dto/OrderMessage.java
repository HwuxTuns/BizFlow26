package com.example.bizflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderMessage {
    
    private Long orderId;
    private String orderCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<OrderItemMessage> items;
    
    // ==================== CONSTRUCTORS ====================
    
    public OrderMessage() {
    }
    
    public OrderMessage(Long orderId, String orderCode, Long customerId, String customerName, 
                       String customerPhone, BigDecimal totalAmount, Integer itemCount, 
                       String createdBy, LocalDateTime createdAt, List<OrderItemMessage> items) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.items = items;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<OrderItemMessage> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemMessage> items) {
        this.items = items;
    }
    
    // ==================== NESTED CLASS ====================
    
    public static class OrderItemMessage {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        
        public OrderItemMessage() {
        }
        
        public OrderItemMessage(Long productId, String productName, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
