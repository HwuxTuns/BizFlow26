package com.bizflow.adminhomeservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminhomeservice.entity.Branch;
import com.bizflow.adminhomeservice.entity.User;
import com.bizflow.adminhomeservice.repository.BranchRepository;
import com.bizflow.adminhomeservice.repository.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardDataController {
    
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    
    public DashboardDataController(UserRepository userRepository, BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "admin-home-service", "database", "connected"));
    }
    
    @GetMapping("/admin-summary")
    public ResponseEntity<Map<String, Object>> getAdminSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            long staffCount = userRepository.countStaff();
            long branchCount = branchRepository.count();
            
            summary.put("totalUsers", totalUsers);
            summary.put("employeeCount", staffCount);
            summary.put("branchCount", branchCount);
            summary.put("productCount", 0); // TODO: Add product count from catalog_db
            
        } catch (Exception e) {
            summary.put("error", e.getMessage());
            summary.put("totalUsers", 0);
            summary.put("employeeCount", 0);
            summary.put("branchCount", 0);
            summary.put("productCount", 0);
        }
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/recent-users")
    public ResponseEntity<List<Map<String, Object>>> getRecentUsers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<User> users = userRepository.findRecentUsers();
            
            List<Map<String, Object>> result = users.stream()
                    .limit(limit)
                    .map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", user.getId());
                        map.put("username", user.getUsername());
                        map.put("fullName", user.getFullName());
                        map.put("email", user.getEmail());
                        map.put("role", user.getRole().name());
                        map.put("enabled", user.getEnabled());
                        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches() {
        try {
            List<Branch> branches = branchRepository.findAll();
            
            List<Map<String, Object>> result = branches.stream()
                    .map(branch -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", branch.getId());
                        map.put("name", branch.getName());
                        map.put("address", branch.getAddress());
                        map.put("phoneNumber", branch.getPhoneNumber());
                        map.put("active", branch.getActive());
                        map.put("createdAt", branch.getCreatedAt() != null ? branch.getCreatedAt().toString() : null);
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> result = users.stream()
                    .map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", user.getId());
                        map.put("username", user.getUsername());
                        map.put("fullName", user.getFullName());
                        map.put("email", user.getEmail());
                        map.put("phoneNumber", user.getPhoneNumber());
                        map.put("role", user.getRole().name());
                        map.put("enabled", user.getEnabled());
                        map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
                        map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
                        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/users/count")
    public ResponseEntity<Long> getUsersCount() {
        try {
            return ResponseEntity.ok(userRepository.count());
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("fullName", user.getFullName());
            map.put("email", user.getEmail());
            map.put("phoneNumber", user.getPhoneNumber());
            map.put("role", user.getRole().name());
            map.put("enabled", user.getEnabled());
            map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
            map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
            map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User();
            user.setUsername((String) userData.get("username"));
            // Password is stored in different table/service - skip for now
            user.setFullName((String) userData.get("fullName"));
            user.setEmail((String) userData.get("email"));
            user.setPhoneNumber((String) userData.get("phoneNumber"));
            user.setRole(User.UserRole.valueOf((String) userData.get("role")));
            user.setEnabled((Boolean) userData.getOrDefault("enabled", true));
            user.setCreatedAt(java.time.LocalDateTime.now());
            
            User saved = userRepository.save(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("username", saved.getUsername());
            result.put("fullName", saved.getFullName());
            result.put("message", "User created successfully (password should be set via AuthService)");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
