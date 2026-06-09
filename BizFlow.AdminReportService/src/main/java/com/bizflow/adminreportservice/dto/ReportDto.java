package com.bizflow.adminreportservice.dto;

import java.time.Instant;

public class ReportDto {

    private Long id;
    private String code;
    private String name;
    private String frequency;
    private Instant lastRun;
    private String status;

    public ReportDto() {
    }

    public ReportDto(Long id, String code, String name, String frequency, Instant lastRun, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.frequency = frequency;
        this.lastRun = lastRun;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Instant getLastRun() {
        return lastRun;
    }

    public void setLastRun(Instant lastRun) {
        this.lastRun = lastRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
