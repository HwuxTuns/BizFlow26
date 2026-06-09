package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SalesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SalesClient(RestTemplateBuilder builder,
                       @Value("${app.sales.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public List<OrderSnapshot> getOrders(LocalDate startDate, LocalDate endDate) {
        String from = startDate == null ? null : startDate.toString();
        String to = endDate == null ? null : endDate.toString();
        String url = baseUrl + "/api/orders/internal";
        if (from != null || to != null) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            if (from != null) {
                sb.append("fromDate=").append(from);
            }
            if (to != null) {
                if (from != null) {
                    sb.append("&");
                }
                sb.append("toDate=").append(to);
            }
            url = sb.toString();
        }
        try {
            ResponseEntity<OrderSnapshot[]> response = restTemplate.getForEntity(url, OrderSnapshot[].class);
            OrderSnapshot[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<PaymentSnapshot> getPayments(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        String url = baseUrl + "/internal/payments?fromDate=" + startDate + "&toDate=" + endDate;
        try {
            ResponseEntity<PaymentSnapshot[]> response = restTemplate.getForEntity(url, PaymentSnapshot[].class);
            PaymentSnapshot[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<DailyRevenueSummary> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        String url = baseUrl + "/internal/reports/revenue/daily?fromDate=" + startDate + "&toDate=" + endDate;
        try {
            ResponseEntity<DailyRevenueSummary[]> response = restTemplate.getForEntity(
                    url,
                    DailyRevenueSummary[].class
            );
            DailyRevenueSummary[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<DailyProductSummary> getDailyProductSummaries(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        String url = baseUrl + "/internal/reports/revenue/items?fromDate=" + startDate + "&toDate=" + endDate;
        try {
            ResponseEntity<DailyProductSummary[]> response = restTemplate.getForEntity(
                    url,
                    DailyProductSummary[].class
            );
            DailyProductSummary[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<ProductSalesSummary> getTopProductSales(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        String url = baseUrl + "/internal/reports/top-products?fromDate=" + startDate + "&toDate=" + endDate
                + "&limit=" + limit;
        try {
            ResponseEntity<ProductSalesSummary[]> response = restTemplate.getForEntity(
                    url,
                    ProductSalesSummary[].class
            );
            ProductSalesSummary[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public DailyReportSummary getDailyReportSummary(LocalDate date) {
        if (date == null) {
            return new DailyReportSummary();
        }
        String url = baseUrl + "/internal/reports/daily?date=" + date;
        try {
            ResponseEntity<DailyReportSummary> response = restTemplate.getForEntity(
                    url,
                    DailyReportSummary.class
            );
            DailyReportSummary body = response.getBody();
            return body == null ? new DailyReportSummary() : body;
        } catch (Exception ex) {
            return new DailyReportSummary();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8081";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    public static class OrderSnapshot {
        private Long id;
        private BigDecimal totalAmount;
        private java.time.LocalDateTime createdAt;
        private String status;
        private Boolean returnOrder;
        private String orderType;
        private String refundMethod;
        private List<OrderItemSnapshot> items;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getReturnOrder() {
            return returnOrder;
        }

        public void setReturnOrder(Boolean returnOrder) {
            this.returnOrder = returnOrder;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getRefundMethod() {
            return refundMethod;
        }

        public void setRefundMethod(String refundMethod) {
            this.refundMethod = refundMethod;
        }

        public List<OrderItemSnapshot> getItems() {
            return items;
        }

        public void setItems(List<OrderItemSnapshot> items) {
            this.items = items;
        }
    }

    public static class OrderItemSnapshot {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private String productName;
        private String productCode;
        private Long categoryId;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
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

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }

    public static class PaymentSnapshot {
        private Long id;
        private String method;
        private BigDecimal amount;
        private java.time.LocalDateTime paidAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public java.time.LocalDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(java.time.LocalDateTime paidAt) {
            this.paidAt = paidAt;
        }
    }

    public static class DailyRevenueSummary {
        private String date;
        private BigDecimal revenue;
        private long orderCount;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(long orderCount) {
            this.orderCount = orderCount;
        }
    }

    public static class DailyProductSummary {
        private String date;
        private Long productId;
        private long quantitySold;
        private BigDecimal revenue;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public long getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(long quantitySold) {
            this.quantitySold = quantitySold;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }

    public static class ProductSalesSummary {
        private Long productId;
        private long quantitySold;
        private BigDecimal revenue;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public long getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(long quantitySold) {
            this.quantitySold = quantitySold;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }

    public static class DailyReportSummary {
        private BigDecimal salesRevenue;
        private BigDecimal returnRevenue;
        private BigDecimal debtIncrease;
        private long salesQuantity;
        private long returnQuantity;
        private long totalInvoices;
        private long salesInvoices;
        private long returnInvoices;
        private long exchangeInvoices;
        private Map<String, BigDecimal> incomeByMethod;
        private Map<String, BigDecimal> expenseByMethod;
        private Map<String, Long> paymentCountByMethod;

        public DailyReportSummary() {
        }

        public BigDecimal getSalesRevenue() {
            return salesRevenue;
        }

        public void setSalesRevenue(BigDecimal salesRevenue) {
            this.salesRevenue = salesRevenue;
        }

        public BigDecimal getReturnRevenue() {
            return returnRevenue;
        }

        public void setReturnRevenue(BigDecimal returnRevenue) {
            this.returnRevenue = returnRevenue;
        }

        public BigDecimal getDebtIncrease() {
            return debtIncrease;
        }

        public void setDebtIncrease(BigDecimal debtIncrease) {
            this.debtIncrease = debtIncrease;
        }

        public long getSalesQuantity() {
            return salesQuantity;
        }

        public void setSalesQuantity(long salesQuantity) {
            this.salesQuantity = salesQuantity;
        }

        public long getReturnQuantity() {
            return returnQuantity;
        }

        public void setReturnQuantity(long returnQuantity) {
            this.returnQuantity = returnQuantity;
        }

        public long getTotalInvoices() {
            return totalInvoices;
        }

        public void setTotalInvoices(long totalInvoices) {
            this.totalInvoices = totalInvoices;
        }

        public long getSalesInvoices() {
            return salesInvoices;
        }

        public void setSalesInvoices(long salesInvoices) {
            this.salesInvoices = salesInvoices;
        }

        public long getReturnInvoices() {
            return returnInvoices;
        }

        public void setReturnInvoices(long returnInvoices) {
            this.returnInvoices = returnInvoices;
        }

        public long getExchangeInvoices() {
            return exchangeInvoices;
        }

        public void setExchangeInvoices(long exchangeInvoices) {
            this.exchangeInvoices = exchangeInvoices;
        }

        public Map<String, BigDecimal> getIncomeByMethod() {
            return incomeByMethod;
        }

        public void setIncomeByMethod(Map<String, BigDecimal> incomeByMethod) {
            this.incomeByMethod = incomeByMethod;
        }

        public Map<String, BigDecimal> getExpenseByMethod() {
            return expenseByMethod;
        }

        public void setExpenseByMethod(Map<String, BigDecimal> expenseByMethod) {
            this.expenseByMethod = expenseByMethod;
        }

        public Map<String, Long> getPaymentCountByMethod() {
            return paymentCountByMethod;
        }

        public void setPaymentCountByMethod(Map<String, Long> paymentCountByMethod) {
            this.paymentCountByMethod = paymentCountByMethod;
        }
    }
}
