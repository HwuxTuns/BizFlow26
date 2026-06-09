package com.example.bizflow.controller;

import com.example.bizflow.dto.DailyReportDTO;
import com.example.bizflow.dto.LowStockAlertDTO;
import com.example.bizflow.dto.RevenueReportDTO;
import com.example.bizflow.dto.ShelfReportDTO;
import com.example.bizflow.dto.TopProductDTO;
import com.example.bizflow.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ==================== DOANH THU & LỢI NHUẬN ====================
    /**
     * Báo cáo doanh thu hôm nay
     */
    @GetMapping("/revenue/today")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<RevenueReportDTO> getTodayRevenue() {
        return ResponseEntity.ok(reportService.getTodayRevenue());
    }

    /**
     * Báo cáo doanh thu tuần này
     */
    @GetMapping("/revenue/weekly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<RevenueReportDTO> getWeeklyRevenue() {
        return ResponseEntity.ok(reportService.getWeeklyRevenue());
    }

    /**
     * Báo cáo doanh thu tháng này
     */
    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<RevenueReportDTO> getMonthlyRevenue() {
        return ResponseEntity.ok(reportService.getMonthlyRevenue());
    }

    /**
     * Báo cáo doanh thu theo khoảng thời gian tùy chọn
     */
    @GetMapping("/revenue/custom")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<RevenueReportDTO> getCustomRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getRevenueReport(startDate, endDate, "custom"));
    }

    /**
     * So sánh doanh thu với kỳ trước (stat changes)
     */
    @GetMapping("/revenue/comparison")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueComparison() {
        return ResponseEntity.ok(reportService.getRevenueComparison());
    }

    // ==================== BÁO CÁO THEO NGÀY ====================
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<DailyReportDTO> getDailyReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(reportService.getDailyReport(targetDate));
    }

    // ==================== TOP SẢN PHẨM BÁN CHẠY ====================
    /**
     * Top 10 sản phẩm bán chạy hôm nay
     */
    @GetMapping("/top-products/today")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsToday(
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.getTopSellingProducts(today, today, limit));
    }

    /**
     * Top sản phẩm bán chạy tuần này
     */
    @GetMapping("/top-products/weekly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsWeekly(
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return ResponseEntity.ok(reportService.getTopSellingProducts(startOfWeek, today, limit));
    }

    /**
     * Top sản phẩm bán chạy tháng này
     */
    @GetMapping("/top-products/monthly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsMonthly(
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return ResponseEntity.ok(reportService.getTopSellingProducts(startOfMonth, today, limit));
    }

    /**
     * Top sản phẩm bán chạy theo khoảng thời gian tùy chọn
     */
    @GetMapping("/top-products/custom")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsCustom(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopSellingProducts(startDate, endDate, limit));
    }

    // ==================== CẢNH BÁO TỒN KHO THẤP ====================
    /**
     * Danh sách sản phẩm tồn kho thấp
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<LowStockAlertDTO>> getLowStockAlerts(
            @RequestParam(required = false) Integer threshold) {
        return ResponseEntity.ok(reportService.getLowStockAlerts(threshold));
    }

    /**
     * Tổng quan cảnh báo tồn kho
     */
    @GetMapping("/low-stock/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> getLowStockSummary(
            @RequestParam(required = false) Integer threshold) {
        return ResponseEntity.ok(reportService.getLowStockSummary(threshold));
    }

    // ==================== BÁO CÁO KỆ HÀNG (OWNER) ====================
    
    /**
     * Danh sách sản phẩm trên kệ hàng
     * Nếu có threshold, chỉ lấy những sản phẩm có quantity < threshold
     */
    @GetMapping("/shelf-stock")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ShelfReportDTO>> getShelfReport(
            @RequestParam(required = false) Integer threshold) {
        return ResponseEntity.ok(reportService.getShelfReport(threshold));
    }

    /**
     * Tổng quan cảnh báo kệ hàng
     */
    @GetMapping("/shelf-stock/summary")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, Long>> getShelfStockSummary(
            @RequestParam(required = false) Integer threshold) {
        return ResponseEntity.ok(reportService.getShelfStockSummary(threshold));
    }
}
