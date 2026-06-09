package com.example.bizflow.service;

import com.example.bizflow.repository.CustomerRepository;
import com.example.bizflow.repository.PointHistoryRepository;
import com.example.bizflow.entity.Customer;
import com.example.bizflow.entity.CustomerTier;
import com.example.bizflow.entity.PointHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PointService {
    private final CustomerRepository customerRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(CustomerRepository customerRepository, PointHistoryRepository pointHistoryRepository) {
        this.customerRepository = customerRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Transactional
    public int addPoints(Long customerId, BigDecimal totalAmount, String reference) {
        if (customerId == null || totalAmount == null) {
            return 0;
        }

        int points = totalAmount.divide(BigDecimal.valueOf(1000), java.math.RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return 0;
        }

        return customerRepository.findByIdForUpdate(customerId).map(customer -> {
            // Prevent duplicate awards for the same reference (atomic check while holding row lock)
            String ref = reference == null ? "ORDER" : reference;
            if (ref != null && !ref.isBlank() && pointHistoryRepository.existsByCustomerIdAndReason(customerId, ref)) {
                return 0;
            }

            customer.addPoints(points);
            // update tier based on monthlyPoints
            updateCustomerTier(customer);
            customerRepository.save(customer);

            // record point history within the same transaction for durability
            PointHistory ph = new PointHistory();
            ph.setCustomer(customer);
            ph.setPoints(points);
            ph.setReason(ref);
            pointHistoryRepository.save(ph);

            return points;
        }).orElse(-1);
    }

    @Transactional
    public int redeemPoints(Long customerId, int points, String reference) {
        if (customerId == null || points <= 0) {
            return 0;
        }

        return customerRepository.findByIdForUpdate(customerId).map(customer -> {
            String ref = reference == null ? null : reference;
            if (ref != null && !ref.isBlank() && pointHistoryRepository.existsByCustomerIdAndReason(customerId, ref)) {
                // already redeemed for this reference
                return 0;
            }

            int current = customer.getTotalPoints() == null ? 0 : customer.getTotalPoints();
            int redeem = Math.min(current, points);
            if (redeem <= 0) {
                return 0;
            }
            customer.setTotalPoints(current - redeem);
            Integer monthly = customer.getMonthlyPoints() == null ? 0 : customer.getMonthlyPoints();
            customer.setMonthlyPoints(Math.max(0, monthly - redeem));
            customerRepository.save(customer);

            // record redeem in point history as negative points (include reference for idempotency)
            PointHistory ph = new PointHistory();
            ph.setCustomer(customer);
            ph.setPoints(-redeem);
            ph.setReason(ref == null ? "REDEEM" : ref);
            pointHistoryRepository.save(ph);

            return redeem;
        }).orElse(0);
    }

    /**
     * Tự động nâng hạng thành viên dựa trên điểm tích lũy trong tháng
     */
    private void updateCustomerTier(Customer customer) {
        if (customer == null) {
            return;
        }

        int monthlyPoints = customer.getMonthlyPoints() != null ? customer.getMonthlyPoints() : 0;
        CustomerTier newTier;

        if (monthlyPoints >= 15000) {
            newTier = CustomerTier.KIM_CUONG;
        } else if (monthlyPoints >= 9000) {
            newTier = CustomerTier.BACH_KIM;
        } else if (monthlyPoints >= 3000) {
            newTier = CustomerTier.VANG;
        } else if (monthlyPoints >= 1000) {
            newTier = CustomerTier.BAC;
        } else {
            newTier = CustomerTier.DONG;
        }

        customer.setTier(newTier);
    }
}
