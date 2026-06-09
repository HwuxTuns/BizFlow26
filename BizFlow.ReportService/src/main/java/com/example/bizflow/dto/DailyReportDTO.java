package com.example.bizflow.dto;

import java.math.BigDecimal;

public class DailyReportDTO {
    private String date;
    private String startTime;
    private String endTime;
    private Summary summary;
    private SalesSummary sales;
    private SalesSummary returns;
    private OtherSummary other;
    private CashSummary cash;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public SalesSummary getSales() {
        return sales;
    }

    public void setSales(SalesSummary sales) {
        this.sales = sales;
    }

    public SalesSummary getReturns() {
        return returns;
    }

    public void setReturns(SalesSummary returns) {
        this.returns = returns;
    }

    public OtherSummary getOther() {
        return other;
    }

    public void setOther(OtherSummary other) {
        this.other = other;
    }

    public CashSummary getCash() {
        return cash;
    }

    public void setCash(CashSummary cash) {
        this.cash = cash;
    }

    public static class Summary {
        private BigDecimal netTotal;
        private Breakdown income;
        private Breakdown expense;
        private DebtSummary debt;

        public BigDecimal getNetTotal() {
            return netTotal;
        }

        public void setNetTotal(BigDecimal netTotal) {
            this.netTotal = netTotal;
        }

        public Breakdown getIncome() {
            return income;
        }

        public void setIncome(Breakdown income) {
            this.income = income;
        }

        public Breakdown getExpense() {
            return expense;
        }

        public void setExpense(Breakdown expense) {
            this.expense = expense;
        }

        public DebtSummary getDebt() {
            return debt;
        }

        public void setDebt(DebtSummary debt) {
            this.debt = debt;
        }
    }

    public static class Breakdown {
        private BigDecimal total;
        private BigDecimal cash;
        private BigDecimal card;
        private BigDecimal transfer;
        private BigDecimal voucher;
        private BigDecimal other;

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public BigDecimal getCash() {
            return cash;
        }

        public void setCash(BigDecimal cash) {
            this.cash = cash;
        }

        public BigDecimal getCard() {
            return card;
        }

        public void setCard(BigDecimal card) {
            this.card = card;
        }

        public BigDecimal getTransfer() {
            return transfer;
        }

        public void setTransfer(BigDecimal transfer) {
            this.transfer = transfer;
        }

        public BigDecimal getVoucher() {
            return voucher;
        }

        public void setVoucher(BigDecimal voucher) {
            this.voucher = voucher;
        }

        public BigDecimal getOther() {
            return other;
        }

        public void setOther(BigDecimal other) {
            this.other = other;
        }
    }

    public static class DebtSummary {
        private BigDecimal increase;
        private BigDecimal decrease;
        private BigDecimal total;

        public BigDecimal getIncrease() {
            return increase;
        }

        public void setIncrease(BigDecimal increase) {
            this.increase = increase;
        }

        public BigDecimal getDecrease() {
            return decrease;
        }

        public void setDecrease(BigDecimal decrease) {
            this.decrease = decrease;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }

    public static class SalesSummary {
        private long quantity;
        private BigDecimal value;

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class OtherSummary {
        private long totalInvoices;
        private long salesInvoices;
        private long returnInvoices;
        private long exchangeInvoices;
        private long voucherCount;
        private long discountCount;
        private long cardPaymentCount;

        public long getTotalInvoices() {
            return totalInvoices;
        }

        public void setTotalInvoices(long totalInvoices) {
            this.totalInvoices = totalInvoices;
        }

        public long getSalesInvoices() {
            return salesInvoices;
        }

        public void setSalesInvoices(long salesInvoices) {
            this.salesInvoices = salesInvoices;
        }

        public long getReturnInvoices() {
            return returnInvoices;
        }

        public void setReturnInvoices(long returnInvoices) {
            this.returnInvoices = returnInvoices;
        }

        public long getExchangeInvoices() {
            return exchangeInvoices;
        }

        public void setExchangeInvoices(long exchangeInvoices) {
            this.exchangeInvoices = exchangeInvoices;
        }

        public long getVoucherCount() {
            return voucherCount;
        }

        public void setVoucherCount(long voucherCount) {
            this.voucherCount = voucherCount;
        }

        public long getDiscountCount() {
            return discountCount;
        }

        public void setDiscountCount(long discountCount) {
            this.discountCount = discountCount;
        }

        public long getCardPaymentCount() {
            return cardPaymentCount;
        }

        public void setCardPaymentCount(long cardPaymentCount) {
            this.cardPaymentCount = cardPaymentCount;
        }
    }

    public static class CashSummary {
        private BigDecimal openingCash;
        private BigDecimal cashCollected;
        private BigDecimal cashRefunded;
        private BigDecimal cashNet;
        private BigDecimal expectedHandover;

        public BigDecimal getOpeningCash() {
            return openingCash;
        }

        public void setOpeningCash(BigDecimal openingCash) {
            this.openingCash = openingCash;
        }

        public BigDecimal getCashCollected() {
            return cashCollected;
        }

        public void setCashCollected(BigDecimal cashCollected) {
            this.cashCollected = cashCollected;
        }

        public BigDecimal getCashRefunded() {
            return cashRefunded;
        }

        public void setCashRefunded(BigDecimal cashRefunded) {
            this.cashRefunded = cashRefunded;
        }

        public BigDecimal getCashNet() {
            return cashNet;
        }

        public void setCashNet(BigDecimal cashNet) {
            this.cashNet = cashNet;
        }

        public BigDecimal getExpectedHandover() {
            return expectedHandover;
        }

        public void setExpectedHandover(BigDecimal expectedHandover) {
            this.expectedHandover = expectedHandover;
        }
    }
}
