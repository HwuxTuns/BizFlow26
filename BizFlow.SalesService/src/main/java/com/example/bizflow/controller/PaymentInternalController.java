package com.example.bizflow.controller;

import com.example.bizflow.entity.Payment;
import com.example.bizflow.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/payments")
public class PaymentInternalController {

    private final PaymentRepository paymentRepository;

    public PaymentInternalController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public ResponseEntity<List<PaymentSnapshot>> getPayments(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        if (fromDate != null && !fromDate.isBlank()) {
            from = LocalDate.parse(fromDate.trim()).atStartOfDay();
        }
        if (toDate != null && !toDate.isBlank()) {
            to = LocalDate.parse(toDate.trim()).atTime(LocalTime.MAX);
        }
        if (from == null || to == null) {
            return ResponseEntity.ok(List.of());
        }
        List<Payment> payments = paymentRepository.findByPaidAtBetween(from, to);
        List<PaymentSnapshot> result = payments.stream()
                .map(PaymentSnapshot::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public static class PaymentSnapshot {
        public Long id;
        public String method;
        public java.math.BigDecimal amount;
        public java.time.LocalDateTime paidAt;

        public static PaymentSnapshot fromEntity(Payment payment) {
            PaymentSnapshot snapshot = new PaymentSnapshot();
            snapshot.id = payment.getId();
            snapshot.method = payment.getMethod();
            snapshot.amount = payment.getAmount();
            snapshot.paidAt = payment.getPaidAt();
            return snapshot;
        }
    }
}
