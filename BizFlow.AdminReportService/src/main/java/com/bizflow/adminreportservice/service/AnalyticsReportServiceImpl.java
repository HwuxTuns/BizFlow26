package com.bizflow.adminreportservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;
import com.bizflow.adminreportservice.dto.TopProductDto;

@Service
public class AnalyticsReportServiceImpl implements AnalyticsReportService {

	private static final String DB_SALES = "bizflow_sales_db";
	private static final String DB_INVENTORY = "bizflow_inventory_db";
	private static final String DB_CATALOG = "bizflow_catalog_db";

	private final JdbcTemplate jdbcTemplate;

	public AnalyticsReportServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public SalesOverviewDto salesOverview(LocalDate from, LocalDate to) {
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> params = new ArrayList<>();
		addDateRange(where, params, from, to);

		String sql = ("""
				SELECT
					COUNT(*) AS total_orders,
					SUM(CASE WHEN o.status = 'PAID' THEN 1 ELSE 0 END) AS paid_orders,
					COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN o.total_amount ELSE 0 END), 0) AS revenue
				FROM %s.orders o
				""" + where).formatted(DB_SALES);

		return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
			long totalOrders = rs.getLong("total_orders");
			long paidOrders = rs.getLong("paid_orders");
			BigDecimal revenue = rs.getBigDecimal("revenue");
			if (revenue == null) revenue = BigDecimal.ZERO;
			BigDecimal average = (paidOrders > 0)
					? revenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP)
					: BigDecimal.ZERO;
			return new SalesOverviewDto(totalOrders, paidOrders, revenue, average);
		}, params.toArray());
	}

	@Override
	public List<DailySalesDto> salesDaily(LocalDate from, LocalDate to) {
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> params = new ArrayList<>();
		addDateRange(where, params, from, to);

		String sql = ("""
				SELECT
					DATE(o.created_at) AS d,
					COUNT(*) AS order_count,
					COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN o.total_amount ELSE 0 END), 0) AS revenue
				FROM %s.orders o
				""" + where + " GROUP BY DATE(o.created_at) ORDER BY DATE(o.created_at)")
				.formatted(DB_SALES);

		return jdbcTemplate.query(sql, (rs, rowNum) -> new DailySalesDto(
				rs.getDate("d").toLocalDate(),
				rs.getLong("order_count"),
				rs.getBigDecimal("revenue")
		), params.toArray());
	}

	@Override
	public List<TopProductDto> topProducts(LocalDate from, LocalDate to, int limit) {
		int safeLimit = clamp(limit, 1, 100);

		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> params = new ArrayList<>();
		addDateRange(where, params, from, to);
		// Count revenue only for paid orders
		where.append(" AND o.status = 'PAID' ");

		String sql = ("""
				SELECT
					oi.product_id AS product_id,
					p.product_name AS product_name,
					SUM(oi.quantity) AS qty,
					COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
				FROM %s.order_items oi
				JOIN %s.orders o ON o.id = oi.order_id
				LEFT JOIN %s.products p ON p.product_id = oi.product_id
				""" + where + " GROUP BY oi.product_id, p.product_name ORDER BY qty DESC LIMIT " + safeLimit)
				.formatted(DB_SALES, DB_SALES, DB_CATALOG);

		return jdbcTemplate.query(sql, (rs, rowNum) -> new TopProductDto(
				rs.getLong("product_id"),
				rs.getString("product_name"),
				rs.getLong("qty"),
				rs.getBigDecimal("revenue")
		), params.toArray());
	}

	@Override
	public List<LowStockDto> lowStock(int threshold, int limit) {
		int safeThreshold = Math.max(0, threshold);
		int safeLimit = clamp(limit, 1, 200);

		String sql = ("""
				SELECT
					s.product_id AS product_id,
					p.product_name AS product_name,
					s.stock AS stock
				FROM %s.inventory_stocks s
				LEFT JOIN %s.products p ON p.product_id = s.product_id
				WHERE s.stock <= ?
				ORDER BY s.stock ASC, s.product_id ASC
				LIMIT ?
				""").formatted(DB_INVENTORY, DB_CATALOG);

		return jdbcTemplate.query(sql, (rs, rowNum) -> new LowStockDto(
				rs.getLong("product_id"),
				rs.getString("product_name"),
				rs.getInt("stock")
		), safeThreshold, safeLimit);
	}

	private static void addDateRange(StringBuilder where, List<Object> params, LocalDate from, LocalDate to) {
		if (from != null) {
			where.append(" AND o.created_at >= ? ");
			params.add(Date.valueOf(from));
		}
		if (to != null) {
			where.append(" AND o.created_at < ? ");
			params.add(Date.valueOf(to.plusDays(1)));
		}
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}

