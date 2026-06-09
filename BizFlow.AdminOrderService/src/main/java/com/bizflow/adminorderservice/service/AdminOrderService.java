package com.bizflow.adminorderservice.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bizflow.adminorderservice.dto.OrderDetailDto;
import com.bizflow.adminorderservice.dto.OrderSummaryDto;
import com.bizflow.adminorderservice.request.OrderStatusUpdateRequest;

public interface AdminOrderService {

    List<OrderSummaryDto> listOrders(String status, String query);

    Page<OrderSummaryDto> listOrdersPage(String status, String query, int page, int size);

    OrderDetailDto getOrder(Long id);

    OrderSummaryDto updateOrderStatus(Long id, OrderStatusUpdateRequest request);
}
