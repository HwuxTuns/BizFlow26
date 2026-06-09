package com.example.bizflow.dto;

public class OwnerDashboardSummary {
    private String timestamp;
    private long totalUsers;
    private long totalEmployees;
    private long totalManagers;
    private long totalCustomers;
    private long totalProducts;
    private long totalBranches;

    public OwnerDashboardSummary() {}

    public OwnerDashboardSummary(String timestamp,
                                 long totalUsers,
                                 long totalEmployees,
                                 long totalManagers,
                                 long totalCustomers,
                                 long totalProducts,
                                 long totalBranches) {
        this.timestamp = timestamp;
        this.totalUsers = totalUsers;
        this.totalEmployees = totalEmployees;
        this.totalManagers = totalManagers;
        this.totalCustomers = totalCustomers;
        this.totalProducts = totalProducts;
        this.totalBranches = totalBranches;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getTotalManagers() { return totalManagers; }
    public void setTotalManagers(long totalManagers) { this.totalManagers = totalManagers; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalBranches() { return totalBranches; }
    public void setTotalBranches(long totalBranches) { this.totalBranches = totalBranches; }
}
