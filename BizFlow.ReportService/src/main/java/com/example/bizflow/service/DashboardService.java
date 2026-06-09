package com.example.bizflow.service;

import com.example.bizflow.config.RoutingDataSource;
import com.example.bizflow.dto.AdminDashboardSummary;
import com.example.bizflow.dto.BranchSummary;
import com.example.bizflow.dto.FinancialSummary;
import com.example.bizflow.dto.OwnerDashboardSummary;
import com.example.bizflow.dto.RecentUserSummary;
import com.example.bizflow.dto.UserDetailDto;
import com.example.bizflow.dto.UserSearchItem;
import com.example.bizflow.entity.Branch;
import com.example.bizflow.entity.User;
import com.example.bizflow.integration.CatalogClient;
import com.example.bizflow.integration.SalesClient;
import com.example.bizflow.repository.BranchRepository;
import com.example.bizflow.repository.CustomerRepository;
import com.example.bizflow.repository.ProductRepository;
import com.example.bizflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesClient salesClient;
    private final CatalogClient catalogClient;
    private final TransactionTemplate requiresNewTx;

    public DashboardService(UserRepository userRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SalesClient salesClient,
            CatalogClient catalogClient,
            PlatformTransactionManager multiTransactionManager) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesClient = salesClient;
        this.catalogClient = catalogClient;

        TransactionTemplate template = new TransactionTemplate(
                Objects.requireNonNull(multiTransactionManager, "multiTransactionManager must not be null"));
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(true);
        this.requiresNewTx = template;
    }

    public OwnerDashboardSummary getOwnerSummary() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // IMPORTANT: không đổi database trong cùng 1 transaction vì connection đã được
        // lấy từ lần query đầu tiên.
        // Mỗi database query được chạy trong một transaction REQUIRES_NEW riêng.
        long[] authCounts = runInDatabase("auth", () -> new long[] {
                userRepository.count(),
                userRepository.countByRole("EMPLOYEE"),
                userRepository.countByRole("MANAGER"),
                branchRepository.count()
        });

        long totalCustomers = runInDatabase("customer", customerRepository::count);
        long totalProducts = runInDatabase("catalog", productRepository::count);

        long totalUsers = authCounts[0];
        long totalEmployees = authCounts[1];
        long totalManagers = authCounts[2];
        long totalBranches = authCounts[3];

        return new OwnerDashboardSummary(
                timestamp,
                totalUsers,
                totalEmployees,
                totalManagers,
                totalCustomers,
                totalProducts,
                totalBranches);
    }

    public AdminDashboardSummary getAdminSummary() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        long[] authCounts = runInDatabase("auth", () -> new long[] {
                userRepository.count(),
                userRepository.countByRole("EMPLOYEE"),
                userRepository.countByRole("MANAGER"),
                branchRepository.count(),
                branchRepository.countByIsActive(true)
        });

        long totalProducts = runInDatabase("catalog", productRepository::count);
        long totalCustomers = runInDatabase("customer", customerRepository::count);

        long totalUsers = authCounts[0];
        long totalEmployees = authCounts[1];
        long totalManagers = authCounts[2];
        long totalBranches = authCounts[3];
        long activeBranches = authCounts[4];

        return new AdminDashboardSummary(
                timestamp,
                totalUsers,
                totalEmployees,
                totalManagers,
                totalBranches,
                activeBranches,
                totalProducts,
                totalCustomers);
    }

    public List<RecentUserSummary> getRecentUsers() {
        // Lấy 5 user mới nhất từ auth database
        List<User> users = runInDatabase("auth", userRepository::findAllByOrderByCreatedAtDesc);

        return users.stream()
                .limit(5)
                .map(this::toRecentUserSummary)
                .collect(Collectors.toList());
    }

    public List<BranchSummary> getBranchSummaries() {
        // Lấy tất cả branches và users từ auth database
        Map<String, Object> authData = runInDatabase("auth", () -> {
            Map<String, Object> result = new HashMap<>();
            result.put("branches", branchRepository.findAll());
            result.put("users", userRepository.findAll());
            return result;
        });

        @SuppressWarnings("unchecked")
        List<Branch> branches = (List<Branch>) authData.get("branches");

        @SuppressWarnings("unchecked")
        List<User> allUsers = (List<User>) authData.get("users");

        // Tạo map để lookup owner information
        Map<Long, User> userMap = new HashMap<>();
        for (User user : allUsers) {
            userMap.put(user.getId(), user);
        }

        return branches.stream()
                .map(branch -> toBranchSummary(branch, userMap))
                .collect(Collectors.toList());
    }

    public List<UserSearchItem> getAllUsersForSearch() {
        List<User> users = runInDatabase("auth", userRepository::findAll);
        return users.stream()
                .map(this::toUserSearchItem)
                .collect(Collectors.toList());
    }

        public List<UserSearchItem> searchUsersForSearch(String q, int limit) {
        String keyword = (q == null) ? "" : q.trim();
        int safeLimit = clamp(limit, 1, 50);

        Page<User> page = runInDatabase("auth", () -> userRepository.searchUsers(
            keyword,
            PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));

        return page.getContent().stream()
            .map(this::toUserSearchItem)
            .collect(Collectors.toList());
        }

    public Optional<UserDetailDto> getUserDetail(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return runInDatabase("auth", () -> userRepository.findById(userId).map(this::toUserDetailDto));
    }

    private <T> T runInDatabase(String databaseKey, Supplier<T> action) {
        // IMPORTANT: phải set routing key trước khi transaction bắt đầu,
        // vì DataSourceTransactionManager sẽ acquire connection ngay khi begin.
        RoutingDataSource.setCurrentDatabase(databaseKey);
        try {
            return requiresNewTx.execute(status -> action.get());
        } finally {
            RoutingDataSource.clearCurrentDatabase();
        }
    }

    private RecentUserSummary toRecentUserSummary(User user) {
        String createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        String fullName = user.getFullName() != null ? user.getFullName() : "-";
        String role = user.getRole() != null ? user.getRole() : "-";
        return new RecentUserSummary(
                user.getId(),
                user.getUsername(),
                fullName,
                role,
                createdAt);
    }

    private UserSearchItem toUserSearchItem(User user) {
        String fullName = user.getFullName() != null ? user.getFullName() : "-";
        String role = user.getRole() != null ? user.getRole() : "-";
        return new UserSearchItem(
                user.getId(),
                user.getUsername(),
                fullName,
                user.getEmail(),
                role);
    }

    private UserDetailDto toUserDetailDto(User user) {
        String createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;

        String branchName = null;
        Long branchId = user.getBranchId();
        if (branchId != null) {
            branchName = branchRepository.findById(branchId)
                    .map(Branch::getName)
                    .orElse(null);
        }

        String fullName = user.getFullName() != null ? user.getFullName() : "-";
        String role = user.getRole() != null ? user.getRole() : "-";

        return new UserDetailDto(
                user.getId(),
                user.getUsername(),
                fullName,
                user.getEmail(),
                user.getPhoneNumber(),
                role,
                user.getEnabled() != null ? user.getEnabled() : Boolean.FALSE,
                branchName,
                createdAt);
    }

    private BranchSummary toBranchSummary(Branch branch, Map<Long, User> userMap) {
        String ownerName = "-";
        if (branch.getOwnerId() != null) {
            User owner = userMap.get(branch.getOwnerId());
            if (owner != null) {
                ownerName = owner.getFullName();
                if (ownerName == null || ownerName.isBlank()) {
                    ownerName = owner.getUsername();
                }
            }
        }
        return new BranchSummary(
                branch.getId(),
                branch.getName(),
                ownerName,
                branch.getIsActive(),
                branch.getAddress());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public FinancialSummary getFinancialSummary() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Get current month data
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        // Get daily revenue data from Sales Service
        List<SalesClient.DailyRevenueSummary> dailySummaries = salesClient.getDailyRevenue(startDate, endDate);
        
        // Calculate total revenue
        BigDecimal totalRevenue = dailySummaries.stream()
                .map(SalesClient.DailyRevenueSummary::getRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Estimate expenses (65% of revenue as cost)
        BigDecimal totalExpense = totalRevenue.multiply(new BigDecimal("0.65"))
                .setScale(0, RoundingMode.HALF_UP);
        
        // Calculate profit
        BigDecimal netProfit = totalRevenue.subtract(totalExpense);
        
        // Calculate profit margin
        Double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue()
                : 0.0;
        
        // Convert daily summaries to DTO
        List<FinancialSummary.DailyRevenue> dailyRevenues = dailySummaries.stream()
                .map(summary -> new FinancialSummary.DailyRevenue(
                        summary.getDate(),
                        summary.getRevenue(),
                        summary.getOrderCount()
                ))
                .collect(Collectors.toList());
        
        // Get product sales for category breakdown
        List<SalesClient.DailyProductSummary> productSummaries = 
                salesClient.getDailyProductSummaries(startDate, endDate);
        
        // Group by product and aggregate revenue
        Map<Long, BigDecimal> productRevenueMap = new HashMap<>();
        for (SalesClient.DailyProductSummary ps : productSummaries) {
            Long productId = ps.getProductId();
            BigDecimal revenue = ps.getRevenue() != null ? ps.getRevenue() : BigDecimal.ZERO;
            productRevenueMap.merge(productId, revenue, BigDecimal::add);
        }
        
        // Get category information from catalog service and group by category
        Map<String, BigDecimal> categoryRevenueMap = new HashMap<>();
        
        for (Map.Entry<Long, BigDecimal> entry : productRevenueMap.entrySet()) {
            CatalogClient.ProductSnapshot product = catalogClient.getProduct(entry.getKey());
            if (product != null) {
                // Use categoryId as category name for now (could be enhanced with category table join)
                String categoryName = product.getCategoryId() != null
                        ? "Danh mục " + product.getCategoryId()
                        : "Chưa phân loại";
                categoryRevenueMap.merge(categoryName, entry.getValue(), BigDecimal::add);
            }
        }
        
        // If no products found or no category data, add a default entry
        if (categoryRevenueMap.isEmpty() && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            categoryRevenueMap.put("Sản phẩm chung", totalRevenue);
        }
        
        // Convert to category revenue list with percentages
        List<FinancialSummary.CategoryRevenue> categoryRevenues = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryRevenueMap.entrySet()) {
            Double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue()
                    : 0.0;
            
            categoryRevenues.add(new FinancialSummary.CategoryRevenue(
                    entry.getKey(),
                    entry.getValue(),
                    percentage
            ));
        }
        
        // Sort by revenue descending
        categoryRevenues.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));
        
        return new FinancialSummary(
                timestamp,
                totalRevenue,
                totalExpense,
                netProfit,
                profitMargin,
                dailyRevenues,
                categoryRevenues
        );
    }
}
