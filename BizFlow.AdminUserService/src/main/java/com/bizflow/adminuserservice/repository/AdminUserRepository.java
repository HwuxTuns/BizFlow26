package com.bizflow.adminuserservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bizflow.adminuserservice.entity.AdminUser;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM AdminUser u WHERE u.role IN ('EMPLOYEE', 'MANAGER', 'OWNER')")
    long countStaff();
    
    @Query("SELECT u FROM AdminUser u ORDER BY u.createdAt DESC")
    List<AdminUser> findRecentUsers();

        @EntityGraph(attributePaths = {"branch"})
        @Query("""
            SELECT u FROM AdminUser u
            WHERE (:q IS NULL OR :q = '' OR (
                (u.username IS NOT NULL AND LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')))
             OR (u.fullName IS NOT NULL AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
             OR (u.email IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
             OR (u.phoneNumber IS NOT NULL AND LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :q, '%')))
            ))
              AND (:role IS NULL OR :role = '' OR UPPER(u.role) = UPPER(:role))
              AND (:enabled IS NULL OR u.enabled = :enabled)
              AND (:branchId IS NULL OR (u.branch IS NOT NULL AND u.branch.id = :branchId))
            """)
        Page<AdminUser> searchUsers(
            @Param("q") String q,
            @Param("role") String role,
            @Param("enabled") Boolean enabled,
            @Param("branchId") Long branchId,
            Pageable pageable
        );
}
