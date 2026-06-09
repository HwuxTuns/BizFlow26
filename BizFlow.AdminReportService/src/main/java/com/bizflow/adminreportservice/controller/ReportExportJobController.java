package com.bizflow.adminreportservice.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminreportservice.entity.ReportExportJob;
import com.bizflow.adminreportservice.service.ReportExportJobService;

@RestController
@RequestMapping("/admin/reports/export/jobs")
public class ReportExportJobController {

	private final ReportExportJobService jobService;

	public ReportExportJobController(ReportExportJobService jobService) {
		this.jobService = jobService;
	}

	@PostMapping("/sales")
	public ResponseEntity<JobCreatedResponse> enqueueSales(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "csv") String format) {
		ReportExportJob job = jobService.enqueueSalesExport(from, to, format);
		return ResponseEntity.accepted().body(JobCreatedResponse.from(job));
	}

	@PostMapping("/inventory/low-stock")
	public ResponseEntity<JobCreatedResponse> enqueueLowStock(
			@RequestParam(defaultValue = "10") int threshold,
			@RequestParam(defaultValue = "50") int limit,
			@RequestParam(defaultValue = "csv") String format) {
		ReportExportJob job = jobService.enqueueLowStockExport(threshold, limit, format);
		return ResponseEntity.accepted().body(JobCreatedResponse.from(job));
	}

	@GetMapping("/{jobId}")
	public ResponseEntity<JobStatusResponse> getStatus(@PathVariable String jobId) {
		ReportExportJob job = jobService.getJob(jobId);
		if (job == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(JobStatusResponse.from(job));
	}

	@GetMapping(value = "/{jobId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> download(@PathVariable String jobId) {
		ReportExportJob job = jobService.getJob(jobId);
		if (job == null) return ResponseEntity.notFound().build();

		if (job.getStatus() != ReportExportJob.Status.SUCCEEDED) {
			String msg = "Job not ready. Status=" + job.getStatus();
			if (job.getStatus() == ReportExportJob.Status.FAILED && job.getErrorMessage() != null) {
				msg += " Error=" + job.getErrorMessage();
			}
			return ResponseEntity.status(409)
					.contentType(MediaType.TEXT_PLAIN)
					.body(msg.getBytes(StandardCharsets.UTF_8));
		}

		byte[] bytes = job.getResultBytes();
		if (bytes == null || bytes.length == 0) {
			return ResponseEntity.status(500)
					.contentType(MediaType.TEXT_PLAIN)
					.body("Export result missing".getBytes(StandardCharsets.UTF_8));
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.attachment().filename(job.getFilename() == null ? "report" : job.getFilename()).build());
		MediaType ct = (job.getContentType() == null) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(job.getContentType());
		return ResponseEntity.ok().headers(headers).contentType(ct).body(bytes);
	}

	public record JobCreatedResponse(String jobId, String status) {
		public static JobCreatedResponse from(ReportExportJob job) {
			return new JobCreatedResponse(job.getId(), job.getStatus().name());
		}
	}

	public record JobStatusResponse(String jobId, String status, String filename, String contentType, String error) {
		public static JobStatusResponse from(ReportExportJob job) {
			return new JobStatusResponse(job.getId(), job.getStatus().name(), job.getFilename(), job.getContentType(), job.getErrorMessage());
		}
	}
}
