package com.bizflow.adminuserservice.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.adminuserservice.dto.AdminUserDto;
import com.bizflow.adminuserservice.entity.AdminUser;
import com.bizflow.adminuserservice.entity.Branch;
import com.bizflow.adminuserservice.exception.AdminUserAlreadyExistsException;
import com.bizflow.adminuserservice.exception.AdminUserNotFoundException;
import com.bizflow.adminuserservice.exception.BranchInactiveException;
import com.bizflow.adminuserservice.exception.BranchNotFoundException;
import com.bizflow.adminuserservice.repository.AdminUserRepository;
import com.bizflow.adminuserservice.repository.BranchRepository;
import com.bizflow.adminuserservice.request.AdminUserCreationRequest;
import com.bizflow.adminuserservice.request.AdminUserStatusUpdateRequest;
import com.bizflow.adminuserservice.request.BranchStaffCreationRequest;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(AdminUserRepository adminUserRepository,
                                BranchRepository branchRepository,
                                PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<AdminUserDto> listAdminUsers() {
        return searchAdminUsers(null, null, null, null);
    }

    @Override
    public List<AdminUserDto> searchAdminUsers(String query, String role, Boolean enabled, Long branchId) {
        return adminUserRepository.findAll().stream()
                .filter(user -> matchesQuery(query, user))
                .filter(user -> matchesRole(role, user))
                .filter(user -> matchesEnabled(enabled, user))
                .filter(user -> matchesBranch(branchId, user))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminUserDto createAdminUser(AdminUserCreationRequest request) {
        ensureUniqueCredentials(request);
        AdminUser entity = toEntity(request);
        AdminUser saved = adminUserRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public AdminUserDto getAdminUserById(Long id) {
        return adminUserRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new AdminUserNotFoundException(id));
    }

    @Override
    @Transactional
    public AdminUserDto updateAdminUserStatus(Long id, AdminUserStatusUpdateRequest request) {
        AdminUser existing = adminUserRepository.findById(id)
                .orElseThrow(() -> new AdminUserNotFoundException(id));
        existing.setEnabled(request.getEnabled());
        existing.setNote(request.getNote());
        AdminUser saved = adminUserRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public AdminUserDto createBranchStaff(Long branchId, BranchStaffCreationRequest request) {
        resolveBranch(branchId);
        AdminUserCreationRequest creation = new AdminUserCreationRequest();
        creation.setUsername(request.getUsername());
        creation.setPassword(request.getPassword());
        creation.setEmail(request.getEmail());
        creation.setFullName(request.getFullName());
        creation.setPhoneNumber(request.getPhoneNumber());
        creation.setRole("EMPLOYEE");
        creation.setEnabled(request.getEnabled() != null ? request.getEnabled() : Boolean.TRUE);
        creation.setBranchId(branchId);
        creation.setNote(request.getNote());
        return createAdminUser(creation);
    }

    private AdminUser toEntity(AdminUserCreationRequest request) {
        AdminUser entity = new AdminUser();
        entity.setUsername(request.getUsername());
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setEmail(request.getEmail());
        entity.setFullName(request.getFullName());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setRole(request.getRole());
        entity.setEnabled(request.getEnabled());
        entity.setNote(request.getNote());
        entity.setBranch(resolveBranch(request.getBranchId()));
        return entity;
    }

    private void ensureUniqueCredentials(AdminUserCreationRequest request) {
        adminUserRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new AdminUserAlreadyExistsException("Username " + user.getUsername() + " is already in use");
                });
        adminUserRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new AdminUserAlreadyExistsException("Email " + user.getEmail() + " is already in use");
                });
    }

    private Branch resolveBranch(Long branchId) {
        if (branchId == null) {
            return null;
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
        if (Boolean.FALSE.equals(branch.getActive())) {
            throw new BranchInactiveException(branchId);
        }
        return branch;
    }

    private AdminUserDto toDto(AdminUser entity) {
        Branch branch = entity.getBranch();
        Long branchId = branch != null ? branch.getId() : null;
        String branchName = branch != null ? branch.getName() : null;
        return new AdminUserDto(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getRole(),
                entity.getEnabled(),
                branchId,
                branchName,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getNote()
        );
    }

    private boolean matchesQuery(String query, AdminUser user) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber()
        ).filter(Objects::nonNull)
                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalized));
    }

    private boolean matchesRole(String role, AdminUser user) {
        if (role == null || role.isBlank()) {
            return true;
        }
        return role.equalsIgnoreCase(user.getRole());
    }

    private boolean matchesEnabled(Boolean enabled, AdminUser user) {
        if (enabled == null) {
            return true;
        }
        return enabled.equals(user.getEnabled());
    }

    private boolean matchesBranch(Long branchId, AdminUser user) {
        if (branchId == null) {
            return true;
        }
        Branch branch = user.getBranch();
        return branch != null && Objects.equals(branchId, branch.getId());
    }
    
    @Override
    public AdminUserDto authenticate(String username, String password) {
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        if (!user.getEnabled()) {
            throw new RuntimeException("Account is disabled");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        return toDto(user);
    }
    
    @Override
    public long countUsers() {
        return adminUserRepository.count();
    }
}
