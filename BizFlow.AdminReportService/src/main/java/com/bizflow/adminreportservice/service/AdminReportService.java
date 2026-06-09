package com.bizflow.adminreportservice.service;

import java.util.List;

import com.bizflow.adminreportservice.dto.ReportDto;

public interface AdminReportService {

    List<ReportDto> listReports();

    ReportDto triggerReport(Long id);
}
