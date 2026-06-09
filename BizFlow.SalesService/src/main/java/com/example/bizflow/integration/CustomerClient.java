package com.example.bizflow.integration;

import com.example.bizflow.entity.CustomerTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CustomerClient {

    private static final Logger logger = LoggerFactory.getLogger(CustomerClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CustomerClient(RestTemplateBuilder builder,
                          @Value("${app.customer.base-url:http://localhost:8085}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public CustomerSnapshot getCustomer(Long id) {
        if (id == null) {
            return null;
        }
        try {
            ResponseEntity<CustomerSnapshot> response = restTemplate.getForEntity(
                    baseUrl + "/api/customers/" + id,
                    CustomerSnapshot.class
            );
            return response.getBody();
        } catch (org.springframework.web.client.RestClientException ex) {
            return null;
        }
    }

    public int addPoints(Long customerId, BigDecimal totalAmount, String reference) {
        if (customerId == null || totalAmount == null) {
            return -1;
        }
        try {
            Map<String, Object> payload = Map.of(
                    "customerId", customerId,
                    "totalAmount", totalAmount,
                    "reference", reference
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<java.util.Map<String, Object>> response = restTemplate.postForEntity(
                    baseUrl + "/internal/customers/points/add",
                    entity,
                    (Class<java.util.Map<String, Object>>)(Class<?>)java.util.Map.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                java.util.Map<String, Object> body = response.getBody();
                if (body != null && body.get("pointsAdded") instanceof Number) {
                    return ((Number) body.get("pointsAdded")).intValue();
                }
                // fallback: compute points same as server rule
                int points = totalAmount.divide(BigDecimal.valueOf(1000), java.math.RoundingMode.DOWN).intValue();
                return Math.max(0, points);
            }
            logger.warn("Failed to add points for customer {}: status={} body={}", customerId, response.getStatusCode(), response.getBody());
        } catch (org.springframework.web.client.RestClientException ex) {
            logger.warn("Failed to add points for customer {} due to exception", customerId, ex);
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public int redeemPoints(Long customerId, int points, String reference) {
        if (customerId == null || points <= 0) {
            return 0;
        }
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("customerId", customerId);
            payload.put("points", points);
            if (reference != null) payload.put("reference", reference);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    baseUrl + "/internal/customers/points/redeem",
                    payload,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body != null) {
                Object redeemed = body.get("redeemed");
                if (redeemed instanceof Number redeemedNumber) {
                    return redeemedNumber.intValue();
                }
            }
        } catch (org.springframework.web.client.RestClientException ignored) {
        }
        return 0;
    }

    public List<Long> searchCustomerIds(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        try {
            ResponseEntity<Long[]> response = restTemplate.getForEntity(
                    baseUrl + "/internal/customers/search?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8),
                    Long[].class
            );
            Long[] body = response.getBody();
            return body == null ? Collections.emptyList() : java.util.Arrays.asList(body);
        } catch (org.springframework.web.client.RestClientException ex) {
            return Collections.emptyList();
        }
    }

    public CustomerSnapshot upsertCustomer(String name, String phone, String email, String address) {
        if (phone == null || phone.isBlank()) return null;
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            if (name != null) payload.put("name", name);
            payload.put("phone", phone);
            if (email != null) payload.put("email", email);
            if (address != null) payload.put("address", address);
            ResponseEntity<CustomerSnapshot> response = restTemplate.postForEntity(
                    baseUrl + "/internal/customers/upsert",
                    payload,
                    CustomerSnapshot.class
            );
            return response.getBody();
        } catch (org.springframework.web.client.RestClientException ex) {
            logger.warn("Failed to upsert customer by phone {}", phone, ex);
            return null;
        }
    }

    public CustomerSnapshot upsertCustomerByUser(Long userId, String username, String name, String email, String address) {
        if (userId == null) return null;
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("userId", userId);
            if (username != null) payload.put("username", username);
            if (name != null) payload.put("name", name);
            if (email != null) payload.put("email", email);
            if (address != null) payload.put("address", address);
            ResponseEntity<CustomerSnapshot> response = restTemplate.postForEntity(
                    baseUrl + "/internal/customers/upsertByUser",
                    payload,
                    CustomerSnapshot.class
            );
            return response.getBody();
        } catch (org.springframework.web.client.RestClientException ex) {
            return null;
        }
    }

    public boolean hasPointHistoryForReferences(Long customerId, java.util.List<String> references) {
        if (customerId == null || references == null || references.isEmpty()) return false;
        try {
            ResponseEntity<PointHistoryEntry[]> response = restTemplate.getForEntity(
                    baseUrl + "/internal/customers/" + customerId + "/points/history",
                    PointHistoryEntry[].class
            );
            PointHistoryEntry[] body = response.getBody();
            if (body == null) return false;
            for (PointHistoryEntry ph : body) {
                if (ph == null || ph.getReason() == null) continue;
                if (references.contains(ph.getReason())) return true;
            }
        } catch (org.springframework.web.client.RestClientException ex) {
            logger.warn("Failed to fetch point history for customer {}", customerId, ex);
        }
        return false;
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8085";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    public static class CustomerSnapshot {
        private Long id;
        private String name;
        private String phone;
        private Long userId;
        private String username;
        private Integer totalPoints;
        private Integer monthlyPoints;
        private CustomerTier tier;

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

        public String getPhone() {
            return phone;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(Integer totalPoints) {
            this.totalPoints = totalPoints;
        }

        public Integer getMonthlyPoints() {
            return monthlyPoints;
        }

        public void setMonthlyPoints(Integer monthlyPoints) {
            this.monthlyPoints = monthlyPoints;
        }

        public CustomerTier getTier() {
            return tier;
        }

        public void setTier(CustomerTier tier) {
            this.tier = tier;
        }
    }

    public static class PointHistoryEntry {
        private Long id;
        private Integer points;
        private String reason;
        private String createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
