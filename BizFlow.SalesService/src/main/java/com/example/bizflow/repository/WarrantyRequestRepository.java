package com.example.bizflow.repository;

import com.example.bizflow.entity.WarrantyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarrantyRequestRepository extends JpaRepository<WarrantyRequest, Long> {

    List<WarrantyRequest> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<WarrantyRequest> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<WarrantyRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<WarrantyRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<WarrantyRequest> findAllByOrderByCreatedAtDesc();

    @Query("""
      SELECT w FROM WarrantyRequest w
      WHERE
        (:status IS NULL OR w.status = :status)
        AND (
          :keyword IS NULL
          OR w.invoiceNumber LIKE CONCAT('%', :keyword, '%')
          OR w.customerName LIKE CONCAT('%', :keyword, '%')
          OR w.customerPhone LIKE CONCAT('%', :keyword, '%')
          OR w.productName LIKE CONCAT('%', :keyword, '%')
        )
      ORDER BY w.createdAt DESC
    """)
    List<WarrantyRequest> searchWarrantyRequests(
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    // Kiểm tra yêu cầu bảo hành trùng (cùng đơn + cùng SP + đang xử lý)
    List<WarrantyRequest> findByOrderIdAndProductIdAndStatusIn(Long orderId, Long productId, List<String> statuses);
}
