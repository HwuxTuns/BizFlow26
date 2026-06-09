package com.example.bizflow.controller;

import com.example.bizflow.entity.User;
import com.example.bizflow.service.BranchService;
import com.example.bizflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private BranchService branchService;

    /**
     * Get complete dashboard summary with all statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Get counts
        long totalUsers = userService.getUsersCount();
        long staffCount = userService.getStaffCount();
        long branchCount = branchService.getBranchesCount();

        summary.put("totalUsers", totalUsers);
        summary.put("totalEmployees", staffCount);
        summary.put("totalBranches", branchCount);
        summary.put("activeBranches", branchCount);
        summary.put("totalProducts", 155); // TODO: Get from CatalogService
        summary.put("totalCustomers", 0); // TODO: Get from CustomerService

        return ResponseEntity.ok(summary);
    }

    /**
     * Get recent users with formatted data for display
     */
    @GetMapping("/recent-users")
    public ResponseEntity<List<Map<String, Object>>> getRecentUsers(
            @RequestParam(defaultValue = "10") int limit) {

        List<User> users = userService.getRecentUsers(limit);

        List<Map<String, Object>> formattedUsers = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("fullName", user.getFullName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole().name());
                    userMap.put("roleDisplay", user.getRole().getDisplayName());
                    userMap.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                    userMap.put("enabled", user.getEnabled());

                    if (user.getBranch() != null) {
                        userMap.put("branchName", user.getBranch().getName());
                    }

                    return userMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedUsers);
    }

    /**
     * Get all branches with formatted data
     */
    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches() {
        var branches = branchService.getAllBranches();

        List<Map<String, Object>> formattedBranches = branches.stream()
                .map(branch -> {
                    Map<String, Object> branchMap = new HashMap<>();
                    branchMap.put("id", branch.getId());
                    branchMap.put("name", branch.getName());
                    branchMap.put("address", branch.getAddress());
                    branchMap.put("phone", branch.getPhone());
                    branchMap.put("email", branch.getEmail());
                    branchMap.put("active", branch.getIsActive());

                    if (branch.getOwner() != null) {
                        branchMap.put("ownerName", branch.getOwner().getFullName());
                    }

                    return branchMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedBranches);
    }

    /**
     * Get complete dashboard data in one call
     */
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteDashboard(
            @RequestParam(defaultValue = "10") int userLimit) {

        Map<String, Object> dashboard = new HashMap<>();

        // Summary statistics
        dashboard.put("summary", getDashboardSummary().getBody());

        // Recent users
        dashboard.put("recentUsers", getRecentUsers(userLimit).getBody());

        // All branches
        dashboard.put("branches", getBranches().getBody());

        return ResponseEntity.ok(dashboard);
    }
}
