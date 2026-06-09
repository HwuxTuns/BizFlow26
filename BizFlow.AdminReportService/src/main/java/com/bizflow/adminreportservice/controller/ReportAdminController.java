package com.bizflow.adminreportservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminreportservice.dto.ReportDto;
import com.bizflow.adminreportservice.service.AdminReportService;

@RestController
@RequestMapping("/admin/reports")
public class ReportAdminController {

    private final AdminReportService adminReportService;

    public ReportAdminController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "admin-report-service"));
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> listReports() {
        return ResponseEntity.ok(adminReportService.listReports());
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<ReportDto> trigger(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminReportService.triggerReport(id));
    }
}
