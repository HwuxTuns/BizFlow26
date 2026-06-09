package com.bizflow.adminhomeservice.dto;

import java.time.Instant;

public class HomeMetricDto {

    private Long id;
    private String metricName;
    private String description;
    private Double metricValue;
    private Instant updatedAt;

    public HomeMetricDto() {
    }

    public HomeMetricDto(Long id, String metricName, String description, Double metricValue, Instant updatedAt) {
        this.id = id;
        this.metricName = metricName;
        this.description = description;
        this.metricValue = metricValue;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
