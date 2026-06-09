/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.bizflow.service;

import com.example.bizflow.entity.Order;
import com.example.bizflow.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMM");
    private static final String DEFAULT_BRANCH_PREFIX = "TC";

    private final OrderRepository orderRepository;
    private final String branchPrefix;

    public OrderService(OrderRepository orderRepository,
                        @Value("${app.invoice.branch-prefix:TC}") String branchPrefix) {
        this.orderRepository = orderRepository;
        this.branchPrefix = (branchPrefix == null || branchPrefix.isBlank())
                ? DEFAULT_BRANCH_PREFIX
                : branchPrefix.trim().toUpperCase();
    }

    public String generateInvoiceNumberForDate(LocalDate date) {
        String prefix = branchPrefix + "-" + date.format(INVOICE_DATE_FORMAT);

        Optional<Order> latest = orderRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);
        int nextSequence = 1;
        if (latest.isPresent()) {
            String invoiceNumber = latest.get().getInvoiceNumber();
            if (invoiceNumber != null && invoiceNumber.startsWith(prefix)) {
                String suffix = invoiceNumber.substring(prefix.length());
                try {
                    nextSequence = Integer.parseInt(suffix) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }

        String invoiceNumber = prefix + String.format("%05d", nextSequence);
        while (orderRepository.findByInvoiceNumber(invoiceNumber).isPresent()) {
            nextSequence += 1;
            invoiceNumber = prefix + String.format("%05d", nextSequence);
        }

        return invoiceNumber;
    }

    public Object getCustomerOrderHistory(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(id);
        return orders == null ? Collections.emptyList() : orders;
    }
}
