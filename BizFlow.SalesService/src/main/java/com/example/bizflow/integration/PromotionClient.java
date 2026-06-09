package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PromotionClient(RestTemplateBuilder builder,
                           @Value("${app.promotion.base-url:http://localhost:8082}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public Map<Long, CartItemPriceResponse> calculatePrices(List<CartItem> items) {
        return requestPrices(items, false);
    }

    public Map<Long, CartItemPriceResponse> calculateAndConsumePrices(List<CartItem> items) {
        return requestPrices(items, true);
    }

    private Map<Long, CartItemPriceResponse> requestPrices(List<CartItem> items, boolean consume) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        CartItemPriceRequest request = new CartItemPriceRequest();
        request.setItems(items);
        String endpoint = consume
                ? "/api/v1/promotions/calculate-prices/consume"
                : "/api/v1/promotions/calculate-prices";

        try {
            ResponseEntity<CartItemPriceResponse[]> response = restTemplate.postForEntity(
                    baseUrl + endpoint,
                    request,
                    CartItemPriceResponse[].class
            );
            CartItemPriceResponse[] body = response.getBody();
            if (body == null || body.length == 0) {
                return Collections.emptyMap();
            }

            Map<Long, CartItemPriceResponse> result = new HashMap<>();
            for (CartItemPriceResponse item : body) {
                if (item != null && item.getProductId() != null) {
                    result.put(item.getProductId(), item);
                }
            }
            return result;
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8082";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    public static class CartItemPriceRequest {
        private List<CartItem> items;

        public List<CartItem> getItems() {
            return items;
        }

        public void setItems(List<CartItem> items) {
            this.items = items;
        }
    }

    public static class CartItem {
        private Long productId;
        private Long categoryId;
        private BigDecimal basePrice;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public BigDecimal getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class CartItemPriceResponse {
        private Long productId;
        private BigDecimal basePrice;
        private BigDecimal finalPrice;
        private BigDecimal discount;
        private String promoCode;
        private String promoName;
        private String promoType;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public BigDecimal getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        public BigDecimal getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public void setDiscount(BigDecimal discount) {
            this.discount = discount;
        }

        public String getPromoCode() {
            return promoCode;
        }

        public void setPromoCode(String promoCode) {
            this.promoCode = promoCode;
        }

        public String getPromoName() {
            return promoName;
        }

        public void setPromoName(String promoName) {
            this.promoName = promoName;
        }

        public String getPromoType() {
            return promoType;
        }

        public void setPromoType(String promoType) {
            this.promoType = promoType;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
