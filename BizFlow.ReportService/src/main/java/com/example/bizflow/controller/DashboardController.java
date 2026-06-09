package com.example.bizflow.controller;

import com.example.bizflow.dto.AdminDashboardSummary;
import com.example.bizflow.dto.BranchSummary;
import com.example.bizflow.dto.FinancialSummary;
import com.example.bizflow.dto.OwnerDashboardSummary;
import com.example.bizflow.dto.RecentUserSummary;
import com.example.bizflow.dto.UserDetailDto;
import com.example.bizflow.dto.UserSearchItem;
import com.example.bizflow.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {
    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/owner-summary")
    public ResponseEntity<OwnerDashboardSummary> getOwnerSummary() {
        return ResponseEntity.ok(dashboardService.getOwnerSummary());
    }

    @GetMapping("/admin-summary")
    public ResponseEntity<AdminDashboardSummary> getAdminSummary() {
        return ResponseEntity.ok(dashboardService.getAdminSummary());
    }

    @GetMapping("/financial-summary")
    public ResponseEntity<FinancialSummary> getFinancialSummary() {
        return ResponseEntity.ok(dashboardService.getFinancialSummary());
    }

    @GetMapping("/recent-users")
    public ResponseEntity<List<RecentUserSummary>> getRecentUsers() {
        return ResponseEntity.ok(dashboardService.getRecentUsers());
    }

    @GetMapping("/branches")
    public ResponseEntity<List<BranchSummary>> getBranchSummaries() {
        return ResponseEntity.ok(dashboardService.getBranchSummaries());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSearchItem>> getUsersForSearch(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", required = false) Integer limit) {
        if (q != null && !q.trim().isEmpty()) {
            int l = (limit == null) ? 10 : limit;
            return ResponseEntity.ok(dashboardService.searchUsersForSearch(q, l));
        }
        return ResponseEntity.ok(dashboardService.getAllUsersForSearch());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailDto> getUserDetail(@PathVariable("id") Long id) {
        return dashboardService.getUserDetail(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
