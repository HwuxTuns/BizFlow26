package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryClient(RestTemplateBuilder builder,
                           @Value("${app.inventory.base-url:http://localhost:8084}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    @SuppressWarnings("unchecked")
    public int getStock(Long productId) {
        if (productId == null) {
            return 0;
        }
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
                    baseUrl + "/api/inventory/stock/" + productId,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Object stock = body.get("stock");
                if (stock instanceof Number stockNumber) {
                    return stockNumber.intValue();
                }
            }
        } catch (org.springframework.web.client.RestClientException ex) {
            return 0;
        }
        return 0;
    }

    public boolean applySale(List<SaleItem> items, Long orderId, Long userId) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        try {
            SaleRequest request = new SaleRequest();
            request.setOrderId(orderId);
            request.setUserId(userId);
            request.setItems(items);
            restTemplate.postForEntity(
                    baseUrl + "/internal/inventory/sales",
                    request,
                    Void.class
            );
            return true;
        } catch (org.springframework.web.client.RestClientException ex) {
            return false;
        }
    }

    public List<StockItem> getStocks(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            ResponseEntity<StockItem[]> response = restTemplate.postForEntity(
                    baseUrl + "/internal/inventory/stocks",
                    productIds,
                    StockItem[].class
            );
            StockItem[] body = response.getBody();
            return body == null ? Collections.emptyList() : java.util.Arrays.asList(body);
        } catch (org.springframework.web.client.RestClientException ex) {
            return Collections.emptyList();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8084";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    public static class SaleItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;

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

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    public static class StockItem {
        private Long productId;
        private Integer stock;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }
    }

    public static class SaleRequest {
        private Long orderId;
        private Long userId;
        private List<SaleItem> items;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public List<SaleItem> getItems() {
            return items;
        }

        public void setItems(List<SaleItem> items) {
            this.items = items;
        }
    }
}
