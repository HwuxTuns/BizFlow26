package com.example.bizflow.repository;

import com.example.bizflow.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByToken(String token);
    Optional<Payment> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, String status);
    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
}
