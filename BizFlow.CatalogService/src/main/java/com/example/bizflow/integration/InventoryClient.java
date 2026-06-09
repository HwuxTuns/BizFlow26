package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryClient(RestTemplateBuilder builder,
                           @Value("${app.inventory.base-url:http://localhost:8084}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
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
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (org.springframework.web.client.RestClientException ex) {
            return Collections.emptyList();
        }
    }

    public void receiveStock(Long productId, Integer quantity, BigDecimal unitPrice, String note, Long userId) {
        if (productId == null || quantity == null || quantity <= 0) {
            return;
        }
        try {
            ReceiptRequest request = new ReceiptRequest();
            request.productId = productId;
            request.quantity = quantity;
            request.unitPrice = unitPrice;
            request.note = note;
            request.userId = userId;
            restTemplate.postForEntity(
                    baseUrl + "/internal/inventory/receipts",
                    request,
                    Void.class
            );
        } catch (org.springframework.web.client.RestClientException ignored) {
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8084";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
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

    @SuppressWarnings("unused")
    private static class ReceiptRequest {
        public Long productId;
        public Integer quantity;
        public BigDecimal unitPrice;
        public String note;
        public Long userId;

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

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
