package com.bizflow.adminorderservice.dto;

import java.time.Instant;

public class OrderSummaryDto {

    private Long id;
    private String invoiceNumber;
    private String status;
    private String customerName;
    private Double totalAmount;
    private Instant createdAt;
    private String shippingMethod;
    private String trackingNumber;
    private Instant shippingStartedAt;
    private Instant deliveredAt;
    private String address;

    public OrderSummaryDto() {
    }

    public OrderSummaryDto(Long id, String invoiceNumber, String status, String customerName, Double totalAmount,
                           Instant createdAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.status = status;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public Instant getShippingStartedAt() { return shippingStartedAt; }
    public void setShippingStartedAt(Instant shippingStartedAt) { this.shippingStartedAt = shippingStartedAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
