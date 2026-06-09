package com.bizflow.adminuserservice.service;

import java.util.List;

import com.bizflow.adminuserservice.dto.AdminUserDto;
import com.bizflow.adminuserservice.request.AdminUserCreationRequest;
import com.bizflow.adminuserservice.request.AdminUserStatusUpdateRequest;
import com.bizflow.adminuserservice.request.BranchStaffCreationRequest;

public interface AdminUserService {

    List<AdminUserDto> listAdminUsers();

    List<AdminUserDto> searchAdminUsers(String query, String role, Boolean enabled, Long branchId);

    AdminUserDto createAdminUser(AdminUserCreationRequest request);

    AdminUserDto getAdminUserById(Long id);

    AdminUserDto updateAdminUserStatus(Long id, AdminUserStatusUpdateRequest request);

    AdminUserDto createBranchStaff(Long branchId, BranchStaffCreationRequest request);
    
    AdminUserDto authenticate(String username, String password);
    
    long countUsers();
}
