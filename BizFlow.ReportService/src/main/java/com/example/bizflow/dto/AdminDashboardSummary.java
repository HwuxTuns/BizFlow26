package com.example.bizflow.dto;

public class AdminDashboardSummary {
    private String timestamp;
    private long totalUsers;
    private long totalEmployees;
    private long totalManagers;
    private long totalBranches;
    private long activeBranches;
    private long totalProducts;
    private long totalCustomers;

    public AdminDashboardSummary() {}

    public AdminDashboardSummary(String timestamp,
                                 long totalUsers,
                                 long totalEmployees,
                                 long totalManagers,
                                 long totalBranches,
                                 long activeBranches,
                                 long totalProducts,
                                 long totalCustomers) {
        this.timestamp = timestamp;
        this.totalUsers = totalUsers;
        this.totalEmployees = totalEmployees;
        this.totalManagers = totalManagers;
        this.totalBranches = totalBranches;
        this.activeBranches = activeBranches;
        this.totalProducts = totalProducts;
        this.totalCustomers = totalCustomers;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getTotalManagers() { return totalManagers; }
    public void setTotalManagers(long totalManagers) { this.totalManagers = totalManagers; }

    public long getTotalBranches() { return totalBranches; }
    public void setTotalBranches(long totalBranches) { this.totalBranches = totalBranches; }

    public long getActiveBranches() { return activeBranches; }
    public void setActiveBranches(long activeBranches) { this.activeBranches = activeBranches; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
}
