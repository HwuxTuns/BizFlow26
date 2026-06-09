package com.bizflow.adminreportservice.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;
import com.bizflow.adminreportservice.export.ExportFormat;
import com.bizflow.adminreportservice.export.ReportExportRenderer;
import com.bizflow.adminreportservice.service.AnalyticsReportService;

@RestController
@RequestMapping("/admin/reports/export")
public class ReportExportController {

	private final AnalyticsReportService analyticsReportService;

	public ReportExportController(AnalyticsReportService analyticsReportService) {
		this.analyticsReportService = analyticsReportService;
	}

	@GetMapping(value = "/sales", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> exportSales(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "csv") String format) {
		ExportFormat f = ExportFormat.parse(format);
		SalesOverviewDto overview = analyticsReportService.salesOverview(from, to);
		List<DailySalesDto> daily = analyticsReportService.salesDaily(from, to);

		try {
			byte[] bytes = ReportExportRenderer.renderSales(f, from, to, overview, daily);
			String filename = "sales-report." + f.extension();
			return ResponseEntity.ok()
					.headers(downloadHeaders(filename))
					.contentType(MediaType.parseMediaType(f.contentType()))
					.body(bytes);
		} catch (IOException e) {
			return ResponseEntity.internalServerError()
					.contentType(MediaType.TEXT_PLAIN)
					.body(("Export failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
		}
	}

	@GetMapping(value = "/inventory/low-stock", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> exportLowStock(
			@RequestParam(defaultValue = "10") int threshold,
			@RequestParam(defaultValue = "50") int limit,
			@RequestParam(defaultValue = "csv") String format) {
		ExportFormat f = ExportFormat.parse(format);

		List<LowStockDto> items = analyticsReportService.lowStock(threshold, limit);
		try {
			byte[] bytes = ReportExportRenderer.renderLowStock(f, threshold, limit, items);
			String filename = "low-stock." + f.extension();
			return ResponseEntity.ok()
					.headers(downloadHeaders(filename))
					.contentType(MediaType.parseMediaType(f.contentType()))
					.body(bytes);
		} catch (IOException e) {
			return ResponseEntity.internalServerError()
					.contentType(MediaType.TEXT_PLAIN)
					.body(("Export failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
		}
	}

	private static HttpHeaders downloadHeaders(String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
		return headers;
	}
}

