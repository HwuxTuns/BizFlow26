package com.example.bizflow.repository;

import com.example.bizflow.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    // Count all branches
    @Override
    long count();

    // Count active branches
    long countByIsActive(Boolean isActive);
}
