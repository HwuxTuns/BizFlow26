package com.bizflow.adminreportservice.service;

import java.time.LocalDate;

import com.bizflow.adminreportservice.entity.ReportExportJob;

public interface ReportExportJobService {
	ReportExportJob enqueueSalesExport(LocalDate from, LocalDate to, String format);

	ReportExportJob enqueueLowStockExport(int threshold, int limit, String format);

	ReportExportJob getJob(String jobId);
}
