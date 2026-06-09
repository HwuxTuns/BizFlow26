package com.example.bizflow.repository;

import com.example.bizflow.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetails();

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByInvoiceNumber(String invoiceNumber);

    @Query("""
      SELECT DISTINCT o FROM Order o
      LEFT JOIN FETCH o.items i
      WHERE
        (:keyword IS NULL OR o.invoiceNumber LIKE CONCAT('%', :keyword, '%'))
        AND (:customerIds IS NULL OR o.customerId IN :customerIds)
        AND (:phone IS NULL OR o.customerPhone LIKE CONCAT('%', :phone, '%'))
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
      ORDER BY o.createdAt DESC
    """)
    List<Order> searchOrders(@Param("keyword") String keyword,
            @Param("customerIds") List<Long> customerIds,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            @Param("phone") String phone);
}
