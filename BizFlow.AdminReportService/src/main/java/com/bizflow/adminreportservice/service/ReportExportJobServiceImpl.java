package com.bizflow.adminreportservice.service;

import java.time.LocalDate;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.bizflow.adminreportservice.config.ReportExportQueueConfig;
import com.bizflow.adminreportservice.entity.ReportExportJob;
import com.bizflow.adminreportservice.export.ExportFormat;
import com.bizflow.adminreportservice.repository.ReportExportJobRepository;

@Service
public class ReportExportJobServiceImpl implements ReportExportJobService {

	private final ReportExportJobRepository jobRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	public ReportExportJobServiceImpl(ReportExportJobRepository jobRepository, KafkaTemplate<String, String> kafkaTemplate) {
		this.jobRepository = jobRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public ReportExportJob enqueueSalesExport(LocalDate from, LocalDate to, String format) {
		ExportFormat f = ExportFormat.parse(format);
		ReportExportJob job = new ReportExportJob();
		job.setJobType(ReportExportJob.JobType.SALES);
		job.setFormat(f.extension());
		job.setFromDate(from);
		job.setToDate(to);
		job.setFilename("sales-report." + f.extension());
		job.setContentType(f.contentType());
		ReportExportJob saved = jobRepository.save(job);
		kafkaTemplate.send(ReportExportQueueConfig.REPORT_EXPORT_TOPIC, saved.getId());
		return saved;
	}

	@Override
	public ReportExportJob enqueueLowStockExport(int threshold, int limit, String format) {
		ExportFormat f = ExportFormat.parse(format);
		ReportExportJob job = new ReportExportJob();
		job.setJobType(ReportExportJob.JobType.LOW_STOCK);
		job.setFormat(f.extension());
		job.setThreshold(threshold);
		job.setLimitValue(limit);
		job.setFilename("low-stock." + f.extension());
		job.setContentType(f.contentType());
		ReportExportJob saved = jobRepository.save(job);
		kafkaTemplate.send(ReportExportQueueConfig.REPORT_EXPORT_TOPIC, saved.getId());
		return saved;
	}

	@Override
	public ReportExportJob getJob(String jobId) {
		return jobRepository.findById(jobId).orElse(null);
	}
}
