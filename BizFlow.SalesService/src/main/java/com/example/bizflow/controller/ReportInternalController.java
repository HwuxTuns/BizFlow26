package com.example.bizflow.controller;

import com.example.bizflow.service.ReportQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/internal/reports")
public class ReportInternalController {

    private final ReportQueryService reportQueryService;

    public ReportInternalController(ReportQueryService reportQueryService) {
        this.reportQueryService = reportQueryService;
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<ReportQueryService.DailyRevenueRow>> getDailyRevenue(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        return ResponseEntity.ok(reportQueryService.getDailyRevenue(from, to));
    }

    @GetMapping("/revenue/items")
    public ResponseEntity<List<ReportQueryService.DailyProductRow>> getDailyProductQuantities(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        return ResponseEntity.ok(reportQueryService.getDailyProductQuantities(from, to));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ReportQueryService.ProductSalesRow>> getTopProducts(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        int capped = limit <= 0 ? 10 : Math.min(limit, 100);
        return ResponseEntity.ok(reportQueryService.getTopProducts(from, to, capped));
    }

    @GetMapping("/daily")
    public ResponseEntity<ReportQueryService.DailyReportSnapshot> getDailyReport(
            @RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        return ResponseEntity.ok(reportQueryService.getDailyReport(targetDate));
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw.trim());
    }
}
