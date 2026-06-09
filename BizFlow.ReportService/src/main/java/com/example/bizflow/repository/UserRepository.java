package com.example.bizflow.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.bizflow.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Count users by role
    long countByRole(String role);

    // Count all users
    @Override
    long count();

    // Find users by role
    List<User> findByRole(String role);

    // Find all users ordered by creation date descending
    List<User> findAllByOrderByCreatedAtDesc();

    // Find top N recent users
    @Query(value = "SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers();

    @Query("""
            SELECT u
            FROM User u
            WHERE (:q IS NULL OR :q = ''
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<User> searchUsers(@Param("q") String q, Pageable pageable);
}
