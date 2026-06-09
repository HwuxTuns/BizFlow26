package com.example.bizflow.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportQueryService {

    private final JdbcTemplate jdbcTemplate;

    public ReportQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DailyRevenueRow> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return List.of();
        }
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        String sql = """
            SELECT DATE(o.created_at) AS order_date,
                   COUNT(*) AS order_count,
                   COALESCE(SUM(o.total_amount), 0) AS revenue
            FROM orders o
            WHERE o.status = 'PAID'
              AND (o.is_return IS NULL OR o.is_return = 0)
              AND o.created_at BETWEEN ? AND ?
            GROUP BY DATE(o.created_at)
            ORDER BY DATE(o.created_at)
            """;

        return jdbcTemplate.query(sql, ps -> {
            ps.setObject(1, from);
            ps.setObject(2, to);
        }, (rs, rowNum) -> {
            DailyRevenueRow row = new DailyRevenueRow();
            Date date = rs.getDate("order_date");
            row.date = date == null ? null : date.toLocalDate().toString();
            row.orderCount = rs.getLong("order_count");
            row.revenue = rs.getBigDecimal("revenue");
            return row;
        });
    }

    public List<DailyProductRow> getDailyProductQuantities(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return List.of();
        }
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        String sql = """
            SELECT DATE(o.created_at) AS order_date,
                   oi.product_id AS product_id,
                   COALESCE(SUM(oi.quantity), 0) AS quantity_sold,
                   COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            WHERE o.status = 'PAID'
              AND (o.is_return IS NULL OR o.is_return = 0)
              AND o.created_at BETWEEN ? AND ?
            GROUP BY DATE(o.created_at), oi.product_id
            ORDER BY DATE(o.created_at)
            """;

        return jdbcTemplate.query(sql, ps -> {
            ps.setObject(1, from);
            ps.setObject(2, to);
        }, (rs, rowNum) -> {
            DailyProductRow row = new DailyProductRow();
            Date date = rs.getDate("order_date");
            row.date = date == null ? null : date.toLocalDate().toString();
            row.productId = rs.getLong("product_id");
            row.quantitySold = rs.getLong("quantity_sold");
            row.revenue = rs.getBigDecimal("revenue");
            return row;
        });
    }

    public List<ProductSalesRow> getTopProducts(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate == null || endDate == null || limit <= 0) {
            return List.of();
        }
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        String sql = """
            SELECT oi.product_id AS product_id,
                   COALESCE(SUM(oi.quantity), 0) AS quantity_sold,
                   COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            WHERE o.status = 'PAID'
              AND (o.is_return IS NULL OR o.is_return = 0)
              AND o.created_at BETWEEN ? AND ?
            GROUP BY oi.product_id
            ORDER BY quantity_sold DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, ps -> {
            ps.setObject(1, from);
            ps.setObject(2, to);
            ps.setInt(3, limit);
        }, (rs, rowNum) -> {
            ProductSalesRow row = new ProductSalesRow();
            row.productId = rs.getLong("product_id");
            row.quantitySold = rs.getLong("quantity_sold");
            row.revenue = rs.getBigDecimal("revenue");
            return row;
        });
    }

    public DailyReportSnapshot getDailyReport(LocalDate date) {
        if (date == null) {
            return new DailyReportSnapshot();
        }
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(LocalTime.MAX);

        String orderAggSql = """
            SELECT
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND (o.is_return IS NULL OR o.is_return = 0) THEN o.total_amount ELSE 0 END), 0) AS sales_revenue,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND o.is_return = 1 THEN o.total_amount ELSE 0 END), 0) AS return_revenue,
                COALESCE(SUM(CASE WHEN o.status = 'UNPAID' AND (o.is_return IS NULL OR o.is_return = 0) THEN o.total_amount ELSE 0 END), 0) AS debt_increase,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN 1 ELSE 0 END), 0) AS paid_orders,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND (o.is_return IS NULL OR o.is_return = 0) THEN 1 ELSE 0 END), 0) AS paid_sales,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND o.is_return = 1 THEN 1 ELSE 0 END), 0) AS paid_returns,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND o.is_return = 1 AND UPPER(o.order_type) = 'EXCHANGE' THEN 1 ELSE 0 END), 0) AS exchange_count
            FROM orders o
            WHERE o.created_at BETWEEN ? AND ?
            """;

        OrderAggregate orderAggregate = jdbcTemplate.queryForObject(orderAggSql, (rs, rowNum) -> {
            OrderAggregate agg = new OrderAggregate();
            agg.salesRevenue = rs.getBigDecimal("sales_revenue");
            agg.returnRevenue = rs.getBigDecimal("return_revenue");
            agg.debtIncrease = rs.getBigDecimal("debt_increase");
            agg.paidOrders = rs.getLong("paid_orders");
            agg.paidSales = rs.getLong("paid_sales");
            agg.paidReturns = rs.getLong("paid_returns");
            agg.exchangeCount = rs.getLong("exchange_count");
            return agg;
        }, from, to);

        String qtySql = """
            SELECT
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND (o.is_return IS NULL OR o.is_return = 0) THEN oi.quantity ELSE 0 END), 0) AS sales_qty,
                COALESCE(SUM(CASE WHEN o.status = 'PAID' AND o.is_return = 1 THEN oi.quantity ELSE 0 END), 0) AS return_qty
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            WHERE o.created_at BETWEEN ? AND ?
            """;

        QuantityAggregate qtyAggregate = jdbcTemplate.queryForObject(qtySql, (rs, rowNum) -> {
            QuantityAggregate agg = new QuantityAggregate();
            agg.salesQty = rs.getLong("sales_qty");
            agg.returnQty = rs.getLong("return_qty");
            return agg;
        }, from, to);

        String paymentSql = """
            SELECT
                CASE
                    WHEN p.method IS NULL OR TRIM(p.method) = '' THEN 'OTHER'
                    WHEN UPPER(TRIM(p.method)) IN ('CASH','CARD','TRANSFER','VOUCHER') THEN UPPER(TRIM(p.method))
                    ELSE 'OTHER'
                END AS method,
                COALESCE(SUM(p.amount), 0) AS amount,
                COUNT(*) AS count
            FROM payments p
            WHERE p.paid_at BETWEEN ? AND ?
            GROUP BY method
            """;

        Map<String, BigDecimal> incomeByMethod = new HashMap<>();
        Map<String, Long> paymentCountByMethod = new HashMap<>();
        jdbcTemplate.query(paymentSql, ps -> {
            ps.setObject(1, from);
            ps.setObject(2, to);
        }, rs -> {
            String method = rs.getString("method");
            incomeByMethod.put(method, rs.getBigDecimal("amount"));
            paymentCountByMethod.put(method, rs.getLong("count"));
        });

        String refundSql = """
            SELECT
                CASE
                    WHEN o.refund_method IS NULL OR TRIM(o.refund_method) = '' THEN 'OTHER'
                    WHEN UPPER(TRIM(o.refund_method)) IN ('CASH','CARD','TRANSFER','VOUCHER') THEN UPPER(TRIM(o.refund_method))
                    ELSE 'OTHER'
                END AS method,
                COALESCE(SUM(o.total_amount), 0) AS amount
            FROM orders o
            WHERE o.status = 'PAID'
              AND o.is_return = 1
              AND o.created_at BETWEEN ? AND ?
            GROUP BY method
            """;

        Map<String, BigDecimal> expenseByMethod = new HashMap<>();
        jdbcTemplate.query(refundSql, ps -> {
            ps.setObject(1, from);
            ps.setObject(2, to);
        }, rs -> {
            String method = rs.getString("method");
            expenseByMethod.put(method, rs.getBigDecimal("amount"));
        });

        DailyReportSnapshot snapshot = new DailyReportSnapshot();
        if (orderAggregate != null) {
            snapshot.salesRevenue = orderAggregate.salesRevenue;
            snapshot.returnRevenue = orderAggregate.returnRevenue;
            snapshot.debtIncrease = orderAggregate.debtIncrease;
            snapshot.totalInvoices = orderAggregate.paidOrders;
            snapshot.salesInvoices = orderAggregate.paidSales;
            snapshot.returnInvoices = orderAggregate.paidReturns;
            snapshot.exchangeInvoices = orderAggregate.exchangeCount;
        }
        if (qtyAggregate != null) {
            snapshot.salesQuantity = qtyAggregate.salesQty;
            snapshot.returnQuantity = qtyAggregate.returnQty;
        }
        snapshot.incomeByMethod = incomeByMethod;
        snapshot.expenseByMethod = expenseByMethod;
        snapshot.paymentCountByMethod = paymentCountByMethod;
        return snapshot;
    }

    public static class DailyRevenueRow {
        public String date;
        public BigDecimal revenue;
        public long orderCount;
    }

    public static class DailyProductRow {
        public String date;
        public Long productId;
        public long quantitySold;
        public BigDecimal revenue;
    }

    public static class ProductSalesRow {
        public Long productId;
        public long quantitySold;
        public BigDecimal revenue;
    }

    public static class DailyReportSnapshot {
        public BigDecimal salesRevenue = BigDecimal.ZERO;
        public BigDecimal returnRevenue = BigDecimal.ZERO;
        public BigDecimal debtIncrease = BigDecimal.ZERO;
        public long salesQuantity = 0;
        public long returnQuantity = 0;
        public long totalInvoices = 0;
        public long salesInvoices = 0;
        public long returnInvoices = 0;
        public long exchangeInvoices = 0;
        public Map<String, BigDecimal> incomeByMethod = new HashMap<>();
        public Map<String, BigDecimal> expenseByMethod = new HashMap<>();
        public Map<String, Long> paymentCountByMethod = new HashMap<>();
    }

    private static class OrderAggregate {
        BigDecimal salesRevenue;
        BigDecimal returnRevenue;
        BigDecimal debtIncrease;
        long paidOrders;
        long paidSales;
        long paidReturns;
        long exchangeCount;
    }

    private static class QuantityAggregate {
        long salesQty;
        long returnQty;
    }
}
