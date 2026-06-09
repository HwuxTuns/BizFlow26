package com.example.bizflow.repository;

import com.example.bizflow.entity.WarrantyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarrantyHistoryRepository extends JpaRepository<WarrantyHistory, Long> {
    List<WarrantyHistory> findByWarrantyRequestIdOrderByCreatedAtDesc(Long warrantyRequestId);
}
