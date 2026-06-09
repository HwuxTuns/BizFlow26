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

    public java.util.List<ProductSnapshot> getProducts() {
        try {
            ResponseEntity<ProductSnapshot[]> response = restTemplate.getForEntity(
                    baseUrl + "/api/products",
                    ProductSnapshot[].class
            );
            ProductSnapshot[] body = response.getBody();
            return body == null ? java.util.Collections.emptyList() : java.util.Arrays.asList(body);
        } catch (Exception ex) {
            return java.util.Collections.emptyList();
        }
    }

    public BigDecimal getCostPrice(Long productId) {
        if (productId == null) {
            return BigDecimal.ZERO;
        }
        try {
            ResponseEntity<ProductCostSnapshot> response = restTemplate.getForEntity(
                    baseUrl + "/api/product-costs/" + productId,
                    ProductCostSnapshot.class
            );
            ProductCostSnapshot body = response.getBody();
            if (body != null && body.getCostPrice() != null) {
                return body.getCostPrice();
            }
        } catch (Exception ignored) {
        }
        ProductSnapshot product = getProduct(productId);
        return product == null || product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
    }

    public List<ProductCostSummary> getProductCostSummaries(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            ResponseEntity<ProductCostSummary[]> response = restTemplate.postForEntity(
                    baseUrl + "/internal/catalog/products/batch",
                    ids,
                    ProductCostSummary[].class
            );
            ProductCostSummary[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
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
        private Long categoryId;
        private String code;
        private String barcode;
        private String name;
        private String unit;
        private BigDecimal price;
        private BigDecimal costPrice;

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

        public BigDecimal getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(BigDecimal costPrice) {
            this.costPrice = costPrice;
        }
    }

    public static class ProductCostSnapshot {
        private Long productId;
        private BigDecimal costPrice;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public BigDecimal getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(BigDecimal costPrice) {
            this.costPrice = costPrice;
        }
    }

    public static class ProductCostSummary {
        private Long id;
        private Long categoryId;
        private String code;
        private String name;
        private BigDecimal costPrice;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(BigDecimal costPrice) {
            this.costPrice = costPrice;
        }
    }
}
