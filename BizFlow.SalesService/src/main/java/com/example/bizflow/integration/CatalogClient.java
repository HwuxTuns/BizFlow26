package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

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
                    baseUrl + "/api/products/" + id,
                    ProductSnapshot.class
            );
            return response.getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8083";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    public static class ProductSnapshot {
        private Long id;
        private String name;
        private String code;
        private String barcode;
        private String unit;
        private BigDecimal price;
        private Long categoryId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        private Integer warrantyPeriod;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public Integer getWarrantyPeriod() {
            return warrantyPeriod;
        }

        public void setWarrantyPeriod(Integer warrantyPeriod) {
            this.warrantyPeriod = warrantyPeriod;
        }
    }
}
