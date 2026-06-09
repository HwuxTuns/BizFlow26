package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CatalogClient(RestTemplateBuilder builder,
                         @Value("${app.catalog.base-url:http://localhost:8083}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public ProductSnapshot getProduct(Long id) {
        if (id == null) {
            return null;
        }
        try {
            ResponseEntity<ProductSnapshot> response = restTemplate.getForEntity(
                    baseUrl + "/internal/catalog/products/" + id,
                    ProductSnapshot.class
            );
            return response.getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    public List<ProductSnapshot> getProducts() {
        try {
            ResponseEntity<ProductSnapshot[]> response = restTemplate.getForEntity(
                    baseUrl + "/internal/catalog/products",
                    ProductSnapshot[].class
            );
            ProductSnapshot[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public void updateProductStock(Long id, Integer stock) {
        if (id == null || stock == null || stock < 0) {
            return;
        }
        try {
            StockUpdateRequest request = new StockUpdateRequest();
            request.stock = stock;
            restTemplate.put(baseUrl + "/internal/catalog/products/" + id + "/stock", request);
        } catch (Exception ignored) {
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8083";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    private static class StockUpdateRequest {
        public Integer stock;
    }

    public static class ProductSnapshot {
        private Long id;
        private Long categoryId;
        private String code;
        private String barcode;
        private String name;
        private Double price;
        private String unit;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
