package com.example.bizflow.controller;

import com.example.bizflow.entity.PointHistory;
import com.example.bizflow.repository.CustomerRepository;
import com.example.bizflow.repository.PointHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/customers/{customerId}/points")
public class PointsController {

    private final CustomerRepository customerRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointsController(CustomerRepository customerRepository, PointHistoryRepository pointHistoryRepository) {
        this.customerRepository = customerRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPoints(@PathVariable Long customerId) {
        return customerRepository.findById(customerId)
                .map(c -> ResponseEntity.ok(Map.of("totalPoints", c.getTotalPoints(), "monthlyPoints", c.getMonthlyPoints())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getHistory(@PathVariable Long customerId) {
        List<PointHistory> list = pointHistoryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<Map<String, Object>> out = new ArrayList<>();
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
}
