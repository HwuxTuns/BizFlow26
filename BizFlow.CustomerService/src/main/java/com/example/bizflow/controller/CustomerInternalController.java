package com.example.bizflow.controller;

import com.example.bizflow.entity.Customer;
import com.example.bizflow.entity.PointHistory;
import com.example.bizflow.repository.CustomerRepository;
import com.example.bizflow.repository.PointHistoryRepository;
import com.example.bizflow.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/internal/customers")
public class CustomerInternalController {

    private final PointService pointService;
    private final CustomerRepository customerRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public CustomerInternalController(PointService pointService,
                                      CustomerRepository customerRepository,
                                      PointHistoryRepository pointHistoryRepository) {
        this.pointService = pointService;
        this.customerRepository = customerRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @PostMapping("/points/add")
    public ResponseEntity<Map<String, Object>> addPoints(@RequestBody PointAddRequest request) {
        if (request == null || request.customerId == null || request.totalAmount == null) {
            return ResponseEntity.badRequest().build();
        }
        // Avoid awarding points twice for the same reason
        String ref = request.reference == null ? "ORDER" : request.reference;
        try {
            if (ref != null && !ref.isBlank() && pointHistoryRepository.existsByCustomerIdAndReason(request.customerId, ref)) {
                return ResponseEntity.ok(Map.of("message", "Points already awarded for this reference", "pointsAdded", 0));
            }
        } catch (Exception ignore) {
        }
        int added = pointService.addPoints(request.customerId, request.totalAmount, request.reference);
        if (added < 0) {
            return ResponseEntity.status(404).body(Map.<String, Object>of("message", "Customer not found"));
        }

        // Return authoritative totals so callers can update UI immediately
        return customerRepository.findById(request.customerId)
                .map(c -> ResponseEntity.ok(Map.<String, Object>of(
                        "message", "Points added",
                        "pointsAdded", added,
                        "totalPoints", c.getTotalPoints(),
                        "monthlyPoints", c.getMonthlyPoints()
                )))
                .orElseGet(() -> ResponseEntity.ok(Map.<String, Object>of("message", "Points added", "pointsAdded", added)));
    }

    @PostMapping("/points/redeem")
    public ResponseEntity<Map<String, Integer>> redeemPoints(@RequestBody PointRedeemRequest request) {
        if (request == null || request.customerId == null || request.points == null) {
            return ResponseEntity.badRequest().build();
        }
        String ref = request.reference == null ? null : request.reference;
        try {
            if (ref != null && !ref.isBlank() && pointHistoryRepository.existsByCustomerIdAndReason(request.customerId, ref)) {
            // Already redeemed for this reference
            return customerRepository.findById(request.customerId)
                .map(c -> ResponseEntity.ok(Map.<String, Integer>of(
                    "redeemed", 0,
                    "totalPoints", c.getTotalPoints() == null ? 0 : c.getTotalPoints(),
                    "monthlyPoints", c.getMonthlyPoints() == null ? 0 : c.getMonthlyPoints()
                )))
                .orElseGet(() -> ResponseEntity.ok(Map.<String, Integer>of("redeemed", 0)));
            }
        } catch (Exception ignore) {
        }

        int redeemed = pointService.redeemPoints(request.customerId, request.points, ref);
        final int redeemedFinal = redeemed;
        return customerRepository.findById(request.customerId)
            .map(c -> ResponseEntity.ok(Map.<String, Integer>of(
                "redeemed", redeemedFinal,
                "totalPoints", c.getTotalPoints() == null ? 0 : c.getTotalPoints(),
                "monthlyPoints", c.getMonthlyPoints() == null ? 0 : c.getMonthlyPoints()
            )))
            .orElseGet(() -> ResponseEntity.ok(Map.<String, Integer>of("redeemed", redeemedFinal)));
    }

    @PostMapping("/{customerId}/points/reconcile")
    public ResponseEntity<?> reconcilePoints(@PathVariable Long customerId) {
        if (customerId == null) return ResponseEntity.badRequest().body("customerId required");
        return customerRepository.findById(customerId).map(c -> {
            Integer total = pointHistoryRepository.sumPointsByCustomerId(customerId);
            if (total == null) total = 0;
            java.time.LocalDate now = java.time.LocalDate.now();
            Integer monthly = pointHistoryRepository.sumPointsByCustomerIdAndYearMonth(customerId, now.getYear(), now.getMonthValue());
            if (monthly == null) monthly = 0;
            c.setTotalPoints(Math.max(0, total));
            c.setMonthlyPoints(Math.max(0, monthly));
            // recalc tier
            int monthlyPoints = c.getMonthlyPoints() != null ? c.getMonthlyPoints() : 0;
            com.example.bizflow.entity.CustomerTier newTier;
            if (monthlyPoints >= 15000) newTier = com.example.bizflow.entity.CustomerTier.KIM_CUONG;
            else if (monthlyPoints >= 9000) newTier = com.example.bizflow.entity.CustomerTier.BACH_KIM;
            else if (monthlyPoints >= 3000) newTier = com.example.bizflow.entity.CustomerTier.VANG;
            else if (monthlyPoints >= 1000) newTier = com.example.bizflow.entity.CustomerTier.BAC;
            else newTier = com.example.bizflow.entity.CustomerTier.DONG;
            c.setTier(newTier);
            customerRepository.save(c);
            return ResponseEntity.ok(Map.of("message", "Reconciled points", "totalPoints", c.getTotalPoints(), "monthlyPoints", c.getMonthlyPoints()));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Long>> searchCustomers(@RequestParam(name = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        String trimmed = keyword.trim();
        // If the keyword contains digits, treat it as a phone search and normalize digits-only.
        // Prefer exact normalized phone match to avoid ambiguous partial matches.
        if (trimmed.matches(".*\\d.*")) {
            String digits = normalizePhone(trimmed);
            Optional<Customer> exactMatch = customerRepository.findByPhone(digits);
            if (exactMatch.isPresent()) {
                return ResponseEntity.ok(List.of(exactMatch.get().getId()));
            }
            return ResponseEntity.ok(customerRepository.searchCustomerIds(digits));
        }
        return ResponseEntity.ok(customerRepository.searchCustomerIds(trimmed));
    }

    @PostMapping("/upsert")
    public ResponseEntity<?> upsertCustomer(@RequestBody UpsertRequest request) {
        if (request == null || request.phone == null || request.phone.isBlank()) {
            return ResponseEntity.badRequest().body("Phone is required");
        }
        String raw = request.phone.trim();
        String phone = normalizePhone(raw);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body("Phone is required");
        }
        // try find existing by normalized phone first, then fallback to raw
        java.util.Optional<com.example.bizflow.entity.Customer> found = customerRepository.findByPhone(phone);
        if (found.isEmpty()) {
            found = customerRepository.findByPhone(raw);
        }
        // try find existing by exact phone
        return found
                .map(existing -> {
                    boolean changed = false;
                    if (request.name != null && !request.name.isBlank()) {
                        existing.setName(request.name.trim());
                        changed = true;
                    }
                    if (request.email != null) {
                        existing.setEmail(trimToNull(request.email));
                        changed = true;
                    }
                    if (request.address != null) {
                        existing.setAddress(trimToNull(request.address));
                        changed = true;
                    }
                    // ensure phone is stored normalized
                    if (existing.getPhone() == null || !existing.getPhone().equals(phone)) {
                        existing.setPhone(phone);
                        changed = true;
                    }
                    if (changed) customerRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> {
                    String name = request.name != null && !request.name.isBlank() ? request.name.trim() : "Khách hàng";
                    com.example.bizflow.entity.Customer created = new com.example.bizflow.entity.Customer(name, phone);
                    created.setEmail(trimToNull(request.email));
                    created.setAddress(trimToNull(request.address));
                    com.example.bizflow.entity.Customer saved = customerRepository.save(created);
                    return ResponseEntity.ok(saved);
                });
    }

    @PostMapping("/upsertByUser")
    public ResponseEntity<?> upsertCustomerByUser(@RequestBody UpsertByUserRequest request) {
        if (request == null || request.userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }

        // Try find by userId first, then existing customer records by phone, username or email
        java.util.Optional<com.example.bizflow.entity.Customer> found = customerRepository.findByUserId(request.userId);
        if (found.isEmpty() && request.phone != null && !request.phone.isBlank()) {
            String normalizedPhone = normalizePhone(request.phone.trim());
            if (normalizedPhone != null && !normalizedPhone.isBlank()) {
                found = customerRepository.findByPhone(normalizedPhone);
            }
        }
        if (found.isEmpty() && request.username != null && !request.username.isBlank()) {
            found = customerRepository.findByUsernameIgnoreCase(request.username.trim());
        }
        if (found.isEmpty() && request.email != null && !request.email.isBlank()) {
            found = customerRepository.findByEmailIgnoreCase(request.email.trim());
        }
        if (found.isEmpty() && request.name != null && !request.name.isBlank()) {
            found = customerRepository.findByNameIgnoreCase(request.name.trim());
        }

        return found
                .map(existing -> {
                    boolean changed = false;
                    if (request.name != null && !request.name.isBlank()) {
                        existing.setName(request.name.trim());
                        changed = true;
                    }
                    if (request.email != null) {
                        existing.setEmail(trimToNull(request.email));
                        changed = true;
                    }
                    if (request.address != null) {
                        existing.setAddress(trimToNull(request.address));
                        changed = true;
                    }
                    if (request.phone != null && !request.phone.isBlank()) {
                        String normalizedPhone = normalizePhone(request.phone.trim());
                        if (normalizedPhone != null && !normalizedPhone.isBlank()
                                && (existing.getPhone() == null || !existing.getPhone().equals(normalizedPhone))) {
                            existing.setPhone(normalizedPhone);
                            changed = true;
                        }
                    }
                    if (existing.getUserId() == null || !existing.getUserId().equals(request.userId)) {
                        existing.setUserId(request.userId);
                        changed = true;
                    }
                    if (request.username != null && !request.username.isBlank()
                            && (existing.getUsername() == null || !existing.getUsername().equals(request.username.trim()))) {
                        existing.setUsername(request.username.trim());
                        changed = true;
                    }
                    if (changed) customerRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> {
                    String name = request.name != null && !request.name.isBlank() ? request.name.trim() : "Khách hàng";
                    String phone = null;
                    if (request.phone != null && !request.phone.isBlank()) {
                        phone = normalizePhone(request.phone.trim());
                    }
                    com.example.bizflow.entity.Customer created = new com.example.bizflow.entity.Customer(name, phone);
                    created.setUserId(request.userId);
                    created.setUsername(trimToNull(request.username));
                    created.setEmail(trimToNull(request.email));
                    created.setAddress(trimToNull(request.address));
                    com.example.bizflow.entity.Customer saved = customerRepository.save(created);
                    return ResponseEntity.ok(saved);
                });
    }

    @GetMapping("/{customerId}/points/history")
    public ResponseEntity<?> getInternalPointHistory(@PathVariable Long customerId) {
        if (customerId == null) {
            return ResponseEntity.badRequest().body("customerId is required");
        }
        List<PointHistory> list = pointHistoryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (PointHistory ph : list) {
            out.add(Map.of(
                "id", ph.getId(),
                "points", ph.getPoints(),
                "reason", ph.getReason(),
                "createdAt", ph.getCreatedAt() != null ? ph.getCreatedAt().toString() : null
            ));
        }
        return ResponseEntity.ok(out);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
    }

    private static class UpsertRequest {
        public String name;
        public String phone;
        public String email;
        public String address;
    }

    private static class UpsertByUserRequest {
        public Long userId;
        public String username;
        public String name;
        public String email;
        public String address;
        public String phone;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static class PointAddRequest {
        public Long customerId;
        public BigDecimal totalAmount;
        public String reference;
    }

    private static class PointRedeemRequest {
        public Long customerId;
        public Integer points;
        public String reference;
    }
}
