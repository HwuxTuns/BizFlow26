package com.bizflow.adminuserservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bizflow.adminuserservice.entity.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    @Query("SELECT COUNT(b) FROM Branch b WHERE b.active = true")
    long countActive();
}
