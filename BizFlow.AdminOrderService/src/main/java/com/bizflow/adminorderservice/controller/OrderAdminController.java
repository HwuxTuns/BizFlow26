package com.bizflow.adminorderservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.adminorderservice.dto.OrderDetailDto;
import com.bizflow.adminorderservice.dto.OrderSummaryDto;
import com.bizflow.adminorderservice.request.OrderStatusUpdateRequest;
import com.bizflow.adminorderservice.service.AdminOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/orders")
public class OrderAdminController {

    private final AdminOrderService adminOrderService;

    public OrderAdminController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "admin-order-service"));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> listOrders(@RequestParam(required = false) String status,
                                                            @RequestParam(required = false) String q,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;

            Page<OrderSummaryDto> result = adminOrderService.listOrdersPage(status, q, p, s);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
            headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
            headers.add("X-Page", String.valueOf(result.getNumber()));
            headers.add("X-Page-Size", String.valueOf(result.getSize()));

            return ResponseEntity.ok().headers(headers).body(result.getContent());
        }

        return ResponseEntity.ok(adminOrderService.listOrders(status, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDto> getOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminOrderService.getOrder(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderSummaryDto> updateStatus(@PathVariable("id") Long id,
                                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, request));
    }
}
