package com.bizflow.adminreportservice.service;

import java.time.LocalDate;
import java.util.List;

import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;
import com.bizflow.adminreportservice.dto.TopProductDto;

public interface AnalyticsReportService {

	SalesOverviewDto salesOverview(LocalDate from, LocalDate to);

	List<DailySalesDto> salesDaily(LocalDate from, LocalDate to);

	List<TopProductDto> topProducts(LocalDate from, LocalDate to, int limit);

	List<LowStockDto> lowStock(int threshold, int limit);
}

