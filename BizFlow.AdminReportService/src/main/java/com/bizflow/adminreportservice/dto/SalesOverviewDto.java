package com.bizflow.adminreportservice.dto;

import java.math.BigDecimal;

public record SalesOverviewDto(
	long totalOrders,
	long paidOrders,
	BigDecimal revenue,
	BigDecimal averageOrderValue
) {
}

