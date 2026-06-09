package com.bizflow.adminreportservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;
import com.bizflow.adminreportservice.dto.TopProductDto;
import com.bizflow.adminreportservice.service.AnalyticsReportService;

@RestController
@RequestMapping("/admin/reports/analytics")
public class ReportAnalyticsController {

	private final AnalyticsReportService analyticsReportService;

	public ReportAnalyticsController(AnalyticsReportService analyticsReportService) {
		this.analyticsReportService = analyticsReportService;
	}

	@GetMapping("/sales/overview")
	public SalesOverviewDto salesOverview(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return analyticsReportService.salesOverview(from, to);
	}

	@GetMapping("/sales/daily")
	public List<DailySalesDto> salesDaily(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return analyticsReportService.salesDaily(from, to);
	}

	@GetMapping("/sales/top-products")
	public List<TopProductDto> topProducts(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "10") int limit) {
		return analyticsReportService.topProducts(from, to, limit);
	}

	@GetMapping("/inventory/low-stock")
	public List<LowStockDto> lowStock(
			@RequestParam(defaultValue = "10") int threshold,
			@RequestParam(defaultValue = "50") int limit) {
		return analyticsReportService.lowStock(threshold, limit);
	}
}

