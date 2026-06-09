package com.example.bizflow.service;

import com.example.bizflow.dto.DailyReportDTO;
import com.example.bizflow.dto.LowStockAlertDTO;
import com.example.bizflow.dto.RevenueReportDTO;
import com.example.bizflow.dto.ShelfReportDTO;
import com.example.bizflow.dto.TopProductDTO;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.integration.InventoryClient;
import com.example.bizflow.integration.SalesClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SalesClient salesClient;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int CRITICAL_STOCK_THRESHOLD = 5;

    private final Map<Long, BigDecimal> costPriceCache = new HashMap<>();

    public ReportService(SalesClient salesClient,
                         CatalogClient catalogClient,
                         InventoryClient inventoryClient) {
        this.salesClient = salesClient;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    public void clearCostPriceCache() {
        costPriceCache.clear();
    }

    public RevenueReportDTO getRevenueReport(LocalDate startDate, LocalDate endDate, String period) {
        List<SalesClient.DailyRevenueSummary> dailySummaries = salesClient.getDailyRevenue(startDate, endDate);
        List<SalesClient.DailyProductSummary> dailyProductSummaries = salesClient.getDailyProductSummaries(startDate, endDate);

        RevenueReportDTO report = new RevenueReportDTO();
        report.setPeriod(period);
        report.setStartDate(startDate.format(DateTimeFormatter.ISO_DATE));
        report.setEndDate(endDate.format(DateTimeFormatter.ISO_DATE));

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        long totalItemsSold = 0;

        Map<LocalDate, SalesClient.DailyRevenueSummary> dailyRevenueMap = new HashMap<>();
        for (SalesClient.DailyRevenueSummary summary : dailySummaries) {
            if (summary.getDate() == null) {
                continue;
            }
            dailyRevenueMap.put(LocalDate.parse(summary.getDate()), summary);
        }

        Map<LocalDate, BigDecimal> dailyCostMap = new HashMap<>();
        Map<Long, BigDecimal> costPriceMap = loadCostPrices(dailyProductSummaries);
        for (SalesClient.DailyProductSummary item : dailyProductSummaries) {
            if (item.getDate() == null || item.getProductId() == null) {
                continue;
            }
            LocalDate date = LocalDate.parse(item.getDate());
            long qty = item.getQuantitySold();
            totalItemsSold += qty;
            BigDecimal costPrice = costPriceMap.getOrDefault(item.getProductId(), BigDecimal.ZERO);
            BigDecimal itemCost = costPrice.multiply(BigDecimal.valueOf(qty));
            dailyCostMap.merge(date, itemCost, BigDecimal::add);
        }

        List<RevenueReportDTO.DailyRevenueItem> dailyBreakdown = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            SalesClient.DailyRevenueSummary summary = dailyRevenueMap.get(date);
            BigDecimal dayRevenue = summary == null ? BigDecimal.ZERO : safeAmount(summary.getRevenue());
            long orderCount = summary == null ? 0 : summary.getOrderCount();
            BigDecimal dayCost = dailyCostMap.getOrDefault(date, BigDecimal.ZERO);

            totalRevenue = totalRevenue.add(dayRevenue);
            totalCost = totalCost.add(dayCost);

            BigDecimal dayProfit = dayRevenue.subtract(dayCost);
            BigDecimal dayMargin = BigDecimal.ZERO;
            if (dayRevenue.compareTo(BigDecimal.ZERO) > 0) {
                dayMargin = dayProfit.multiply(BigDecimal.valueOf(100))
                        .divide(dayRevenue, 2, RoundingMode.HALF_UP);
            }

            dailyBreakdown.add(new RevenueReportDTO.DailyRevenueItem(
                    date.format(DateTimeFormatter.ISO_DATE),
                    dayRevenue,
                    dayCost,
                    dayProfit,
                    dayMargin,
                    orderCount
            ));
        }

        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        report.setGrossProfit(totalRevenue.subtract(totalCost));
        report.setTotalOrders(dailySummaries.stream().mapToLong(SalesClient.DailyRevenueSummary::getOrderCount).sum());
        report.setTotalItemsSold(totalItemsSold);
        report.setDailyBreakdown(dailyBreakdown);

        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margin = report.getGrossProfit()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
            report.setProfitMargin(margin);
        } else {
            report.setProfitMargin(BigDecimal.ZERO);
        }

        return report;
    }

    public RevenueReportDTO getTodayRevenue() {
        LocalDate today = LocalDate.now();
        return getRevenueReport(today, today, "daily");
    }

    public RevenueReportDTO getWeeklyRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return getRevenueReport(startOfWeek, today, "weekly");
    }

    public RevenueReportDTO getMonthlyRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return getRevenueReport(startOfMonth, today, "monthly");
    }

    public DailyReportDTO getDailyReport(LocalDate date) {
        SalesClient.DailyReportSummary salesSummary = salesClient.getDailyReportSummary(date);

        BigDecimal salesRevenue = safeAmount(salesSummary.getSalesRevenue());
        BigDecimal returnAmount = safeAmount(salesSummary.getReturnRevenue());
        long salesQuantity = salesSummary.getSalesQuantity();
        long returnQuantity = salesSummary.getReturnQuantity();
        BigDecimal debtIncrease = safeAmount(salesSummary.getDebtIncrease());
        BigDecimal debtDecrease = BigDecimal.ZERO;
        Map<String, BigDecimal> incomeByMethod = salesSummary.getIncomeByMethod() == null
                ? new HashMap<>()
                : new HashMap<>(salesSummary.getIncomeByMethod());
        Map<String, BigDecimal> expenseByMethod = salesSummary.getExpenseByMethod() == null
                ? new HashMap<>()
                : new HashMap<>(salesSummary.getExpenseByMethod());

        DailyReportDTO report = new DailyReportDTO();
        report.setDate(date.format(DateTimeFormatter.ISO_DATE));
        report.setStartTime(date.atStartOfDay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        report.setEndTime(date.atTime(23, 59).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        DailyReportDTO.Breakdown income = new DailyReportDTO.Breakdown();
        income.setCash(getAmount(incomeByMethod, "CASH"));
        income.setCard(getAmount(incomeByMethod, "CARD"));
        income.setTransfer(getAmount(incomeByMethod, "TRANSFER"));
        income.setVoucher(getAmount(incomeByMethod, "VOUCHER"));
        income.setOther(getAmount(incomeByMethod, "OTHER"));
        income.setTotal(income.getCash()
                .add(income.getCard())
                .add(income.getTransfer())
                .add(income.getVoucher())
                .add(income.getOther()));

        DailyReportDTO.Breakdown expense = new DailyReportDTO.Breakdown();
        expense.setCash(getAmount(expenseByMethod, "CASH"));
        expense.setCard(getAmount(expenseByMethod, "CARD"));
        expense.setTransfer(getAmount(expenseByMethod, "TRANSFER"));
        expense.setVoucher(getAmount(expenseByMethod, "VOUCHER"));
        expense.setOther(getAmount(expenseByMethod, "OTHER"));
        expense.setTotal(expense.getCash()
                .add(expense.getCard())
                .add(expense.getTransfer())
                .add(expense.getVoucher())
                .add(expense.getOther()));

        DailyReportDTO.DebtSummary debt = new DailyReportDTO.DebtSummary();
        debt.setIncrease(debtIncrease);
        debt.setDecrease(debtDecrease);
        debt.setTotal(debtIncrease.subtract(debtDecrease));

        DailyReportDTO.Summary summary = new DailyReportDTO.Summary();
        summary.setIncome(income);
        summary.setExpense(expense);
        summary.setDebt(debt);
        summary.setNetTotal(income.getTotal().subtract(expense.getTotal()));
        report.setSummary(summary);

        DailyReportDTO.SalesSummary sales = new DailyReportDTO.SalesSummary();
        sales.setQuantity(salesQuantity);
        sales.setValue(salesRevenue);
        report.setSales(sales);

        DailyReportDTO.SalesSummary returns = new DailyReportDTO.SalesSummary();
        returns.setQuantity(returnQuantity);
        returns.setValue(returnAmount);
        report.setReturns(returns);

        DailyReportDTO.OtherSummary other = new DailyReportDTO.OtherSummary();
        other.setTotalInvoices(salesSummary.getTotalInvoices());
        other.setSalesInvoices(salesSummary.getSalesInvoices());
        other.setReturnInvoices(salesSummary.getReturnInvoices());
        other.setExchangeInvoices(salesSummary.getExchangeInvoices());
        other.setVoucherCount(getCount(salesSummary.getPaymentCountByMethod(), "VOUCHER"));
        other.setDiscountCount(0);
        other.setCardPaymentCount(getCount(salesSummary.getPaymentCountByMethod(), "CARD"));
        report.setOther(other);

        DailyReportDTO.CashSummary cash = new DailyReportDTO.CashSummary();
        cash.setOpeningCash(BigDecimal.ZERO);
        cash.setCashCollected(income.getCash());
        cash.setCashRefunded(expense.getCash());
        cash.setCashNet(income.getCash().subtract(expense.getCash()));
        cash.setExpectedHandover(cash.getOpeningCash().add(cash.getCashNet()));
        report.setCash(cash);

        return report;
    }

    public Map<String, Object> getRevenueComparison() {
        Map<String, Object> comparison = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        RevenueReportDTO todayReport = getRevenueReport(today, today, "daily");
        RevenueReportDTO yesterdayReport = getRevenueReport(yesterday, yesterday, "daily");

        comparison.put("todayRevenue", todayReport.getTotalRevenue());
        comparison.put("todayOrders", todayReport.getTotalOrders());
        comparison.put("todayProfit", todayReport.getGrossProfit());
        comparison.put("todayMargin", todayReport.getProfitMargin());
        comparison.put("todayChange", calculatePercentChange(
                yesterdayReport.getTotalRevenue(), todayReport.getTotalRevenue()));

        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfLastWeek = startOfWeek.minusDays(1);
        LocalDate startOfLastWeek = endOfLastWeek.minusDays(6);

        RevenueReportDTO thisWeekReport = getRevenueReport(startOfWeek, today, "weekly");
        RevenueReportDTO lastWeekReport = getRevenueReport(startOfLastWeek, endOfLastWeek, "weekly");

        comparison.put("weeklyRevenue", thisWeekReport.getTotalRevenue());
        comparison.put("weeklyOrders", thisWeekReport.getTotalOrders());
        comparison.put("weeklyProfit", thisWeekReport.getGrossProfit());
        comparison.put("weeklyMargin", thisWeekReport.getProfitMargin());
        comparison.put("weeklyChange", calculatePercentChange(
                lastWeekReport.getTotalRevenue(), thisWeekReport.getTotalRevenue()));

        LocalDate startOfMonth = today.withDayOfMonth(1);
        int daysInCurrentPeriod = today.getDayOfMonth();
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonthPeriod = startOfLastMonth.plusDays(daysInCurrentPeriod - 1);

        RevenueReportDTO thisMonthReport = getRevenueReport(startOfMonth, today, "monthly");
        RevenueReportDTO lastMonthReport = getRevenueReport(startOfLastMonth, endOfLastMonthPeriod, "monthly");

        comparison.put("monthlyRevenue", thisMonthReport.getTotalRevenue());
        comparison.put("monthlyOrders", thisMonthReport.getTotalOrders());
        comparison.put("monthlyProfit", thisMonthReport.getGrossProfit());
        comparison.put("monthlyMargin", thisMonthReport.getProfitMargin());
        comparison.put("monthlyChange", calculatePercentChange(
                lastMonthReport.getTotalRevenue(), thisMonthReport.getTotalRevenue()));

        comparison.put("marginChange", calculateMarginChange(
                lastMonthReport.getProfitMargin(), thisMonthReport.getProfitMargin()));

        return comparison;
    }

    private BigDecimal calculatePercentChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return BigDecimal.valueOf(100);
            }
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMarginChange(BigDecimal previousMargin, BigDecimal currentMargin) {
        if (previousMargin == null) previousMargin = BigDecimal.ZERO;
        if (currentMargin == null) currentMargin = BigDecimal.ZERO;
        return currentMargin.subtract(previousMargin).setScale(1, RoundingMode.HALF_UP);
    }

    public List<TopProductDTO> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        List<SalesClient.ProductSalesSummary> productSummaries = salesClient.getTopProductSales(startDate, endDate, limit);
        List<Long> productIds = productSummaries.stream()
                .map(SalesClient.ProductSalesSummary::getProductId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());

        Map<Long, CatalogClient.ProductCostSummary> productMap = catalogClient.getProductCostSummaries(productIds).stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(CatalogClient.ProductCostSummary::getId, p -> p, (a, b) -> a));

        Map<Long, Integer> stockMap = inventoryClient.getStocks(productIds).stream()
                .filter(s -> s.getProductId() != null)
                .collect(Collectors.toMap(InventoryClient.StockItem::getProductId, s -> s.getStock(), (a, b) -> a));

        List<TopProductDTO> results = new ArrayList<>();
        for (SalesClient.ProductSalesSummary summary : productSummaries) {
            Long productId = summary.getProductId();
            if (productId == null) {
                continue;
            }
            CatalogClient.ProductCostSummary product = productMap.get(productId);
            long quantitySold = summary.getQuantitySold();
            BigDecimal totalRevenue = safeAmount(summary.getRevenue());
            BigDecimal costPrice = product == null ? BigDecimal.ZERO : safeAmount(product.getCostPrice());
            BigDecimal totalCost = costPrice.multiply(BigDecimal.valueOf(quantitySold));
            BigDecimal profit = totalRevenue.subtract(totalCost);
            Integer stock = stockMap.get(productId);

            TopProductDTO dto = new TopProductDTO(
                    productId,
                    product == null ? null : product.getName(),
                    product == null ? null : product.getCode(),
                    product == null ? null : product.getCategoryId(),
                    quantitySold,
                    totalRevenue,
                    totalCost,
                    profit,
                    stock
            );
            results.add(dto);
        }

        return results;
    }

    public List<LowStockAlertDTO> getLowStockAlerts(Integer threshold) {
        int limit = threshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : threshold;
        List<InventoryClient.LowStockItem> stocks = inventoryClient.getLowStock(limit);
        List<Long> productIds = stocks.stream()
                .map(InventoryClient.LowStockItem::getProductId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        Map<Long, CatalogClient.ProductCostSummary> productMap = catalogClient.getProductCostSummaries(productIds).stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(CatalogClient.ProductCostSummary::getId, p -> p, (a, b) -> a));
        List<LowStockAlertDTO> result = new ArrayList<>();
        for (InventoryClient.LowStockItem stock : stocks) {
            int current = stock.getStock() == null ? 0 : stock.getStock();
            if (current > limit) {
                continue;
            }
            String level = current <= CRITICAL_STOCK_THRESHOLD ? "CRITICAL" : "WARNING";
            CatalogClient.ProductCostSummary product = productMap.get(stock.getProductId());
            LowStockAlertDTO alert = new LowStockAlertDTO(
                    stock.getProductId(),
                    product == null ? null : product.getName(),
                    product == null ? null : product.getCode(),
                    product == null ? null : product.getCategoryId(),
                    current,
                    limit,
                    level
            );
            result.add(alert);
        }
        return result;
    }

    public Map<String, Long> getLowStockSummary(Integer threshold) {
        int limit = threshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : threshold;
        long critical = 0;
        long warning = 0;
        long total = 0;
        for (LowStockAlertDTO alert : getLowStockAlerts(limit)) {
            total++;
            if ("CRITICAL".equalsIgnoreCase(alert.getAlertLevel())) {
                critical++;
            } else {
                warning++;
            }
        }
        Map<String, Long> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("critical", critical);
        summary.put("warning", warning);
        return summary;
    }

    // ==================== BÁO CÁO KỆ HÀNG ====================
    
    public List<ShelfReportDTO> getShelfReport(Integer threshold) {
        List<InventoryClient.ShelfStockSummary> shelves = inventoryClient.getAllShelfStocks();
        List<ShelfReportDTO> result = new ArrayList<>();
        
        for (InventoryClient.ShelfStockSummary shelf : shelves) {
            Integer qty = shelf.getQuantity();
            int quantity = qty != null ? qty : 0;
            
            // Nếu có threshold, chỉ lấy những sản phẩm có quantity < threshold
            if (threshold != null && quantity >= threshold) {
                continue;
            }
            
            ShelfReportDTO dto = new ShelfReportDTO(
                    shelf.getProductId(),
                    shelf.getProductName(),
                    shelf.getProductCode(),
                    shelf.getCategoryId(),
                    quantity,
                    shelf.getAlertLevel()
            );
            result.add(dto);
        }
        
        return result;
    }
    
    public Map<String, Long> getShelfStockSummary(Integer threshold) {
        int limit = threshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : threshold;
        List<ShelfReportDTO> reports = getShelfReport(limit);
        
        long danger = 0;
        long warning = 0;
        long total = reports.size();
        
        for (ShelfReportDTO report : reports) {
            if ("DANGER".equalsIgnoreCase(report.getAlertLevel())) {
                danger++;
            } else if ("WARNING".equalsIgnoreCase(report.getAlertLevel())) {
                warning++;
            }
        }
        
        Map<String, Long> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("danger", danger);
        summary.put("warning", warning);
        return summary;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal getAmount(Map<String, BigDecimal> map, String key) {
        return map.getOrDefault(key, BigDecimal.ZERO);
    }

    @SuppressWarnings("unused")
    private long getCount(Map<String, Long> map, String key) {
        if (map == null) {
            return 0;
        }
        return map.getOrDefault(key, 0L);
    }

    private Map<Long, BigDecimal> loadCostPrices(List<SalesClient.DailyProductSummary> items) {
        List<Long> productIds = items.stream()
                .map(SalesClient.DailyProductSummary::getProductId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, BigDecimal> costMap = new HashMap<>();
        List<Long> missing = new ArrayList<>();
        for (Long productId : productIds) {
            BigDecimal cached = costPriceCache.get(productId);
            if (cached != null) {
                costMap.put(productId, cached);
            } else {
                missing.add(productId);
            }
        }
        if (!missing.isEmpty()) {
            for (CatalogClient.ProductCostSummary summary : catalogClient.getProductCostSummaries(missing)) {
                if (summary.getId() == null) {
                    continue;
                }
                BigDecimal cost = summary.getCostPrice() == null ? BigDecimal.ZERO : summary.getCostPrice();
                costMap.put(summary.getId(), cost);
                costPriceCache.put(summary.getId(), cost);
            }
        }
        return costMap;
    }
}
