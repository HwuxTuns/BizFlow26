package com.bizflow.adminuserservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminuserservice.entity.AdminUser;
import com.bizflow.adminuserservice.entity.Branch;
import com.bizflow.adminuserservice.repository.AdminUserRepository;
import com.bizflow.adminuserservice.repository.BranchRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8200"}, allowCredentials = "true")
public class DirectDBController {

    private final AdminUserRepository userRepository;
    private final BranchRepository branchRepository;

    public DirectDBController(AdminUserRepository userRepository, BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "admin-user-service", "database", "bizflow_auth_db"));
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUsersCount() {
        return ResponseEntity.ok(userRepository.count());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(@RequestParam(required = false) String q,
                                                                 @RequestParam(required = false) String role,
                                                                 @RequestParam(required = false) Boolean enabled,
                                                                 @RequestParam(required = false) Long branchId,
                                                                 @RequestParam(required = false) Integer page,
                                                                 @RequestParam(required = false) Integer size) {
        boolean usePaging = page != null || size != null;
        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? 20 : Math.min(Math.max(1, size), 200);

        Page<AdminUser> usersPage = userRepository.searchUsers(
                q,
                role,
                enabled,
                branchId,
                PageRequest.of(p, usePaging ? s : Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<Map<String, Object>> result = usersPage.getContent().stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole());
                    map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
                    map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
                    map.put("enabled", user.getEnabled());
                    return map;
                })
                .collect(Collectors.toList());

        if (usePaging) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Count", String.valueOf(usersPage.getTotalElements()));
            headers.add("X-Total-Pages", String.valueOf(usersPage.getTotalPages()));
            headers.add("X-Page", String.valueOf(usersPage.getNumber()));
            headers.add("X-Page-Size", String.valueOf(usersPage.getSize()));
            return ResponseEntity.ok().headers(headers).body(result);
        }

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("phoneNumber", user.getPhoneNumber());
                    map.put("role", user.getRole());
                    map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
                    map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
                    map.put("enabled", user.getEnabled());
                    map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                    map.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
                    map.put("note", user.getNote());
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/staff-count")
    public ResponseEntity<Long> getStaffCount() {
        return ResponseEntity.ok(userRepository.countStaff());
    }

    @GetMapping("/users/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentUsers(@RequestParam(defaultValue = "10") int limit) {
        List<AdminUser> users = userRepository.findRecentUsers();
        
        List<Map<String, Object>> result = users.stream()
                .limit(limit)
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole());
                    map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
                    map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
                    map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
                    map.put("enabled", user.getEnabled());
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches() {
        List<Branch> branches = branchRepository.findAll();
        
        List<Map<String, Object>> result = branches.stream()
                .map(branch -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", branch.getId());
                    map.put("name", branch.getName());
                    map.put("address", branch.getAddress());
                    map.put("email", branch.getEmail());
                    map.put("phone", branch.getPhone());
                    map.put("active", Boolean.TRUE.equals(branch.getActive()));
                    
                    // Get owner name if ownerId exists
                    String ownerName = "-";
                    if (branch.getOwnerId() != null) {
                        ownerName = userRepository.findById(branch.getOwnerId())
                                .map(AdminUser::getFullName)
                                .orElse("-");
                    }
                    map.put("ownerName", ownerName);
                    
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/branches/count")
    public ResponseEntity<Long> getBranchesCount() {
        return ResponseEntity.ok(branchRepository.count());
    }

    @GetMapping("/branches/count-active")
    public ResponseEntity<Long> getActiveBranchesCount() {
        return ResponseEntity.ok(branchRepository.countActive());
    }

    // PATCH /users/{id}/status - Update user status (enable/disable)
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> statusUpdate) {
        AdminUser user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        Boolean enabled = (Boolean) statusUpdate.get("enabled");
        String note = (String) statusUpdate.get("note");
        
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        
        if (note != null && !note.trim().isEmpty()) {
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            String updatedNote = user.getNote() != null ? user.getNote() + "\n" : "";
            updatedNote += "[" + timestamp + "] " + note;
            user.setNote(updatedNote);
        }
        
        AdminUser updatedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedUser.getId());
        response.put("username", updatedUser.getUsername());
        response.put("email", updatedUser.getEmail());
        response.put("fullName", updatedUser.getFullName());
        response.put("phoneNumber", updatedUser.getPhoneNumber());
        response.put("role", updatedUser.getRole());
        response.put("enabled", updatedUser.getEnabled());
        response.put("note", updatedUser.getNote());
        response.put("createdAt", updatedUser.getCreatedAt());
        response.put("updatedAt", updatedUser.getUpdatedAt());
        
        if (updatedUser.getBranch() != null) {
            Map<String, Object> branchInfo = new HashMap<>();
            branchInfo.put("id", updatedUser.getBranch().getId());
            branchInfo.put("name", updatedUser.getBranch().getName());
            response.put("branch", branchInfo);
        }
        
        return ResponseEntity.ok(response);
    }
}
