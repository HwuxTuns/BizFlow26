package com.bizflow.adminreportservice.dto;

public record LowStockDto(
	Long productId,
	String productName,
	Integer stock
) {
}

