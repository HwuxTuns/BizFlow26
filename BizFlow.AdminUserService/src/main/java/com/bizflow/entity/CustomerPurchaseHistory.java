package com.bizflow.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing customer purchase history received from Kafka
 */
@Entity
@Table(name = "customer_purchase_history")
public class CustomerPurchaseHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "order_created_at")
    private LocalDateTime orderCreatedAt;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "order_items_json", columnDefinition = "LONGTEXT")
    private String orderItemsJson; // Store as JSON string for flexibility

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    public CustomerPurchaseHistory() {
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderCreatedAt() { return orderCreatedAt; }
    public void setOrderCreatedAt(LocalDateTime orderCreatedAt) { this.orderCreatedAt = orderCreatedAt; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOrderItemsJson() { return orderItemsJson; }
    public void setOrderItemsJson(String orderItemsJson) { this.orderItemsJson = orderItemsJson; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    @Override
    public String toString() {
        return "CustomerPurchaseHistory{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", orderId=" + orderId +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderCreatedAt=" + orderCreatedAt +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
