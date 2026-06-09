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
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CustomerClient(RestTemplateBuilder builder,
                          @Value("${app.customer.base-url:http://localhost:8085}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public List<CustomerSnapshot> getCustomers() {
        try {
            ResponseEntity<CustomerSnapshot[]> response = restTemplate.getForEntity(
                    baseUrl + "/api/customers",
                    CustomerSnapshot[].class
            );
            CustomerSnapshot[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
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

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
