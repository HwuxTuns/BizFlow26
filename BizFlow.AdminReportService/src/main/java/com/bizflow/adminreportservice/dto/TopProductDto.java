package com.bizflow.adminreportservice.dto;

import java.math.BigDecimal;

public record TopProductDto(
	Long productId,
	String productName,
	long quantitySold,
	BigDecimal revenue
) {
}

