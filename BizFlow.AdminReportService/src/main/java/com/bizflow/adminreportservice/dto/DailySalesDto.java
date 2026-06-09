package com.bizflow.adminreportservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesDto(
	LocalDate date,
	long orderCount,
	BigDecimal revenue
) {
}

