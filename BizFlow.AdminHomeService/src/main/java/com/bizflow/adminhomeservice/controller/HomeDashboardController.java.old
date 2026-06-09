package com.bizflow.adminhomeservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminhomeservice.dto.HomeMetricDto;
import com.bizflow.adminhomeservice.service.AdminHomeService;

@RestController
@RequestMapping("/admin/home")
public class HomeDashboardController {

    private final AdminHomeService adminHomeService;

    public HomeDashboardController(AdminHomeService adminHomeService) {
        this.adminHomeService = adminHomeService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "admin-home-service"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<HomeMetricDto>> metrics() {
        return ResponseEntity.ok(adminHomeService.getDashboardMetrics());
    }
}
