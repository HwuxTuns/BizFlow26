package com.example.bizflow.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SalesClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SalesClient(RestTemplateBuilder builder,
                       @Value("${app.sales.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public Object getCustomerOrderHistory(Long customerId) {
        if (customerId == null) {
            return java.util.Collections.emptyList();
        }
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    baseUrl + "/api/orders/customer/" + customerId,
                    Object.class
            );
            return response.getBody();
        } catch (Exception ex) {
            return java.util.Collections.emptyList();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8081";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }
}
