package com.example.bizflow.repository;

import com.example.bizflow.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<ReturnRequest> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    @Query("""
      SELECT r FROM ReturnRequest r
      WHERE
        (:status IS NULL OR r.status = :status)
        AND (
          :keyword IS NULL
          OR r.invoiceNumber LIKE CONCAT('%', :keyword, '%')
          OR r.customerName LIKE CONCAT('%', :keyword, '%')
          OR r.customerPhone LIKE CONCAT('%', :keyword, '%')
          OR r.productName LIKE CONCAT('%', :keyword, '%')
        )
      ORDER BY r.createdAt DESC
    """)
    List<ReturnRequest> searchReturnRequests(
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}
