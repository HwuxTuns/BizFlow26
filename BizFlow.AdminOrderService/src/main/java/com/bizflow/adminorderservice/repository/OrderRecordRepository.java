package com.bizflow.adminorderservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bizflow.adminorderservice.entity.OrderRecord;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, Long> {

    List<OrderRecord> findByStatusIgnoreCase(String status);

        @Query("""
                        SELECT o FROM OrderRecord o
                        WHERE (:status IS NULL OR :status = '' OR UPPER(o.status) = UPPER(:status))
                            AND (
                                     :q IS NULL OR :q = ''
                                     OR (o.invoiceNumber IS NOT NULL AND LOWER(o.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')))
                                     OR (o.customerName IS NOT NULL AND LOWER(o.customerName) LIKE LOWER(CONCAT('%', :q, '%')))
                            )
                        """)
        Page<OrderRecord> searchOrders(@Param("status") String status, @Param("q") String q, Pageable pageable);
}
