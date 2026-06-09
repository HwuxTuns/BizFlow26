package com.example.bizflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class FinancialSummary {
    private String timestamp;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private Double profitMargin;
    private List<DailyRevenue> dailyRevenues;
    private List<CategoryRevenue> categoryRevenues;

    public FinancialSummary() {}

    public FinancialSummary(String timestamp, BigDecimal totalRevenue, BigDecimal totalExpense, 
                           BigDecimal netProfit, Double profitMargin, 
                           List<DailyRevenue> dailyRevenues, List<CategoryRevenue> categoryRevenues) {
        this.timestamp = timestamp;
        this.totalRevenue = totalRevenue;
        this.totalExpense = totalExpense;
        this.netProfit = netProfit;
        this.profitMargin = profitMargin;
        this.dailyRevenues = dailyRevenues;
        this.categoryRevenues = categoryRevenues;
    }

    // Getters and Setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

    public Double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }

    public List<DailyRevenue> getDailyRevenues() { return dailyRevenues; }
    public void setDailyRevenues(List<DailyRevenue> dailyRevenues) { this.dailyRevenues = dailyRevenues; }

    public List<CategoryRevenue> getCategoryRevenues() { return categoryRevenues; }
    public void setCategoryRevenues(List<CategoryRevenue> categoryRevenues) { this.categoryRevenues = categoryRevenues; }

    public static class DailyRevenue {
        private String date;
        private BigDecimal revenue;
        private long orderCount;

        public DailyRevenue() {}

        public DailyRevenue(String date, BigDecimal revenue, long orderCount) {
            this.date = date;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    }

    public static class CategoryRevenue {
        private String categoryName;
        private BigDecimal revenue;
        private Double percentage;

        public CategoryRevenue() {}

        public CategoryRevenue(String categoryName, BigDecimal revenue, Double percentage) {
            this.categoryName = categoryName;
            this.revenue = revenue;
            this.percentage = percentage;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}
