package com.example.bizflow.repository;

import com.example.bizflow.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // ✅ DÙNG ĐỂ CHỐNG CỘNG ĐIỂM TRÙNG
    boolean existsByCustomerIdAndReason(Long customerId, String reason);

    // Lấy lịch sử điểm theo customer id (mới nhất trước)
    List<PointHistory> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointHistory p WHERE p.customer.id = :customerId")
    Integer sumPointsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointHistory p WHERE p.customer.id = :customerId AND YEAR(p.createdAt) = :year AND MONTH(p.createdAt) = :month")
    Integer sumPointsByCustomerIdAndYearMonth(@Param("customerId") Long customerId, @Param("year") int year, @Param("month") int month);
}
