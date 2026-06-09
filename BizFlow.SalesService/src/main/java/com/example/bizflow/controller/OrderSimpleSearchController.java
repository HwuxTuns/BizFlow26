package com.example.bizflow.controller;

import com.example.bizflow.integration.CustomerClient;
import com.example.bizflow.entity.Order;
import com.example.bizflow.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import com.example.bizflow.integration.CustomerClient.CustomerSnapshot;
import com.example.bizflow.util.PhoneUtils;

@RestController
@RequestMapping("/api/search/orders")
public class OrderSimpleSearchController {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;

    public OrderSimpleSearchController(OrderRepository orderRepository, CustomerClient customerClient) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> searchOrders(
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<Long> customerIds = null;
        String normalizedPhone = phone == null || phone.isBlank() ? null : PhoneUtils.normalize(phone.trim());
        if (normalizedPhone != null && !normalizedPhone.isBlank()) {
            customerIds = customerClient.searchCustomerIds(normalizedPhone);
        }

        List<Order> orders = orderRepository.searchOrders(
            keyword == null || keyword.isBlank() ? null : keyword.trim(),
            customerIds,
            null,
            null,
            normalizedPhone
        );

        // If orders are linked to customers (customerId present) but order.customerPhone is empty,
        // fetch customer snapshots to include customer phone in the response so clients can filter reliably.
        Set<Long> cids = orders.stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, CustomerSnapshot> customerMap = new HashMap<>();
        for (Long cid : cids) {
            try {
                CustomerSnapshot cs = customerClient.getCustomer(cid);
                if (cs != null) customerMap.put(cid, cs);
            } catch (Exception ignored) {
            }
        }

        List<Map<String, Object>> responses = orders.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("invoiceNumber", o.getInvoiceNumber());
            m.put("status", o.getStatus());
            m.put("createdAt", o.getCreatedAt());
            m.put("totalAmount", o.getTotalAmount());
                String phoneFromOrder = o.getCustomerPhone();
                String custPhone = phoneFromOrder != null && !phoneFromOrder.isBlank() ? phoneFromOrder
                    : (o.getCustomerId() == null ? null : (customerMap.get(o.getCustomerId()) == null ? null : customerMap.get(o.getCustomerId()).getPhone()));
                m.put("customerPhone", custPhone);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
