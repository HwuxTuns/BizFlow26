package com.bizflow.adminreportservice.worker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.bizflow.adminreportservice.config.ReportExportQueueConfig;
import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;
import com.bizflow.adminreportservice.entity.ReportExportJob;
import com.bizflow.adminreportservice.export.ExportFormat;
import com.bizflow.adminreportservice.export.ReportExportRenderer;
import com.bizflow.adminreportservice.repository.ReportExportJobRepository;
import com.bizflow.adminreportservice.service.AnalyticsReportService;

@Component
@Profile("worker")
public class ReportExportJobWorker {
	private static final Logger log = LoggerFactory.getLogger(ReportExportJobWorker.class);

	private final ReportExportJobRepository jobRepository;
	private final AnalyticsReportService analyticsReportService;

	public ReportExportJobWorker(ReportExportJobRepository jobRepository, AnalyticsReportService analyticsReportService) {
		this.jobRepository = jobRepository;
		this.analyticsReportService = analyticsReportService;
	}

	@KafkaListener(topics = ReportExportQueueConfig.REPORT_EXPORT_TOPIC)
	public void handle(String jobId) {
		log.info("Received export job message: jobId={}", jobId);
		ReportExportJob job = jobRepository.findById(jobId).orElse(null);
		if (job == null) {
			log.warn("Job not found in DB: jobId={}", jobId);
			return;
		}
		if (job.getStatus() != ReportExportJob.Status.QUEUED) {
			log.info("Job is not QUEUED; skipping. jobId={} status={}", jobId, job.getStatus());
			return;
		}

		try {
			log.info("Starting job. jobId={} type={} format={}", jobId, job.getJobType(), job.getFormat());
			job.setStatus(ReportExportJob.Status.PROCESSING);
			job.setStartedAt(LocalDateTime.now());
			jobRepository.save(job);

			ExportFormat format = ExportFormat.parse(job.getFormat());
			byte[] bytes = switch (job.getJobType()) {
				case SALES -> {
					SalesOverviewDto overview = analyticsReportService.salesOverview(job.getFromDate(), job.getToDate());
					List<DailySalesDto> daily = analyticsReportService.salesDaily(job.getFromDate(), job.getToDate());
					yield ReportExportRenderer.renderSales(format, job.getFromDate(), job.getToDate(), overview, daily);
				}
				case LOW_STOCK -> {
					int threshold = job.getThreshold() == null ? 10 : job.getThreshold();
					int limit = job.getLimitValue() == null ? 50 : job.getLimitValue();
					List<LowStockDto> items = analyticsReportService.lowStock(threshold, limit);
					yield ReportExportRenderer.renderLowStock(format, threshold, limit, items);
				}
			};

			job.setResultBytes(bytes);
			job.setContentType(format.contentType());
			if (job.getFilename() == null || job.getFilename().isBlank()) {
				job.setFilename((job.getJobType() == ReportExportJob.JobType.SALES ? "sales-report" : "low-stock") + "." + format.extension());
			}
			job.setStatus(ReportExportJob.Status.SUCCEEDED);
			job.setFinishedAt(LocalDateTime.now());
			jobRepository.save(job);
			log.info("Job succeeded. jobId={} bytes={} filename={}", jobId, bytes == null ? 0 : bytes.length, job.getFilename());
		} catch (IOException | RuntimeException e) {
			job.setStatus(ReportExportJob.Status.FAILED);
			job.setFinishedAt(LocalDateTime.now());
			job.setErrorMessage(e.getMessage());
			jobRepository.save(job);
			log.error("Job failed. jobId={} error={}", jobId, e.getMessage(), e);
		}
	}
}
