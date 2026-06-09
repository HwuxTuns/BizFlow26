package com.bizflow.adminorderservice.service;

import java.util.List;
import java.time.Instant;
import java.util.*;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.adminorderservice.dto.OrderDetailDto;
import com.bizflow.adminorderservice.dto.OrderItemDto;
import com.bizflow.adminorderservice.dto.OrderPaymentDto;
import com.bizflow.adminorderservice.dto.OrderSummaryDto;
import com.bizflow.adminorderservice.entity.OrderRecord;
import com.bizflow.adminorderservice.exception.OrderNotFoundException;
import com.bizflow.adminorderservice.repository.OrderRecordRepository;
import com.bizflow.adminorderservice.request.OrderStatusUpdateRequest;
import com.bizflow.event.PurchaseEvent;
import com.bizflow.producer.OrderEventProducer;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderServiceImpl.class);
    private final OrderRecordRepository orderRecordRepository;
    private final OrderEventProducer orderEventProducer;
    private final JdbcTemplate jdbcTemplate;

    public AdminOrderServiceImpl(OrderRecordRepository orderRecordRepository,
                                ObjectProvider<OrderEventProducer> orderEventProducerProvider,
                                JdbcTemplate jdbcTemplate) {
        this.orderRecordRepository = orderRecordRepository;
        this.orderEventProducer = orderEventProducerProvider.getIfAvailable();
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDto> listOrders(String status, String query) {
        return orderRecordRepository
            .searchOrders(status, query, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt")))
            .getContent()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> listOrdersPage(String status, String query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 200);

        return orderRecordRepository
            .searchOrders(status, query, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDto getOrder(Long id) {
        try {
            Map<String, Object> orderRow = jdbcTemplate.queryForMap(
                    "SELECT o.id, o.invoice_number, o.status, o.total_amount, o.created_at, " +
                            "o.customer_id, o.user_id, o.customer_name, o.note, o.order_type, o.is_return, " +
                            "o.return_reason, o.return_note, o.refund_method, o.parent_order_id " +
                            "FROM bizflow_sales_db.orders o WHERE o.id = ?",
                    id
            );

            OrderDetailDto dto = new OrderDetailDto();
            dto.setId(((Number) orderRow.get("id")).longValue());
            dto.setInvoiceNumber((String) orderRow.get("invoice_number"));
            dto.setStatus((String) orderRow.get("status"));
            dto.setTotalAmount(orderRow.get("total_amount") == null ? null : ((Number) orderRow.get("total_amount")).doubleValue());
            dto.setCreatedAt(toInstant(orderRow.get("created_at")));

            dto.setCustomerId(toLong(orderRow.get("customer_id")));
            dto.setUserId(toLong(orderRow.get("user_id")));
            dto.setCustomerName((String) orderRow.get("customer_name"));
            dto.setNote((String) orderRow.get("note"));
            dto.setOrderType((String) orderRow.get("order_type"));
            dto.setIsReturn(toBoolean(orderRow.get("is_return")));
            dto.setReturnReason((String) orderRow.get("return_reason"));
            dto.setReturnNote((String) orderRow.get("return_note"));
            dto.setRefundMethod((String) orderRow.get("refund_method"));
            dto.setParentOrderId(toLong(orderRow.get("parent_order_id")));

            List<OrderItemDto> items = jdbcTemplate.query(
                    "SELECT oi.id, oi.product_id, oi.quantity, oi.price, " +
                            "p.product_name, p.sku, p.unit " +
                            "FROM bizflow_sales_db.order_items oi " +
                            "LEFT JOIN bizflow_catalog_db.products p ON p.product_id = oi.product_id " +
                            "WHERE oi.order_id = ? ORDER BY oi.id",
                    (rs, rowNum) -> {
                        OrderItemDto item = new OrderItemDto();
                        item.setId(rs.getLong("id"));
                        long productId = rs.getLong("product_id");
                        item.setProductId(rs.wasNull() ? null : productId);
                        item.setQuantity(rs.getInt("quantity"));
                        item.setPrice(rs.getDouble("price"));
                        item.setProductName(rs.getString("product_name"));
                        item.setSku(rs.getString("sku"));
                        item.setUnit(rs.getString("unit"));

                        Double lineTotal = null;
                        if (!rs.wasNull()) {
                            lineTotal = rs.getDouble("price") * rs.getInt("quantity");
                        }
                        item.setLineTotal(lineTotal);
                        return item;
                    },
                    id
            );
            dto.setItems(items);

            List<OrderPaymentDto> payments = jdbcTemplate.query(
                    "SELECT p.id, p.amount, p.method, p.paid_at, p.status, p.token " +
                            "FROM bizflow_sales_db.payments p WHERE p.order_id = ? ORDER BY p.id",
                    (rs, rowNum) -> {
                        OrderPaymentDto payment = new OrderPaymentDto();
                        payment.setId(rs.getLong("id"));
                        payment.setAmount(rs.getDouble("amount"));
                        payment.setMethod(rs.getString("method"));
                        payment.setStatus(rs.getString("status"));
                        payment.setToken(rs.getString("token"));
                        payment.setPaidAt(toInstant(rs.getTimestamp("paid_at")));
                        return payment;
                    },
                    id
            );
            dto.setPayments(payments);

            if (dto.getCustomerId() != null) {
                try {
                    Map<String, Object> customerRow = jdbcTemplate.queryForMap(
                            "SELECT c.id, c.name, c.phone, c.email FROM bizflow_customer_db.customers c WHERE c.id = ?",
                            dto.getCustomerId()
                    );
                    if (dto.getCustomerName() == null || dto.getCustomerName().isBlank()) {
                        dto.setCustomerName((String) customerRow.get("name"));
                    }
                    dto.setCustomerPhone((String) customerRow.get("phone"));
                    dto.setCustomerEmail((String) customerRow.get("email"));
                } catch (EmptyResultDataAccessException ignored) {
                }
            }

            if (dto.getUserId() != null) {
                try {
                    Map<String, Object> userRow = jdbcTemplate.queryForMap(
                            "SELECT u.id, u.username, u.full_name FROM bizflow_auth_db.users u WHERE u.id = ?",
                            dto.getUserId()
                    );
                    dto.setUsername((String) userRow.get("username"));
                    dto.setUserFullName((String) userRow.get("full_name"));
                } catch (EmptyResultDataAccessException ignored) {
                }
            }

            return dto;
        } catch (EmptyResultDataAccessException e) {
            throw new OrderNotFoundException(id);
        }
    }

    @Override
    @Transactional
    public OrderSummaryDto updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        OrderRecord record = orderRecordRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        String oldStatus = record.getStatus() != null ? record.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
        String newStatus = request.getStatus() != null ? request.getStatus().toUpperCase(Locale.ROOT) : oldStatus;
        
        // Define flexible transition map
        Map<String, List<String>> allowedTransitions = new HashMap<>();
        List<String> earlyStates = Arrays.asList("PENDING", "UNPAID", "PAID", "CONFIRMED", "PROCESSING", "PACKING");
        
        // From any early state, you can go to mostly any other state
        for (String s : earlyStates) {
            allowedTransitions.put(s, Arrays.asList("CONFIRMED", "PROCESSING", "PACKING", "SHIPPING", "SHIPPED", "DELIVERED", "CANCELLED"));
        }
        allowedTransitions.put("SHIPPING", Arrays.asList("SHIPPED", "DELIVERED", "CANCELLED", "RETURNED"));
        allowedTransitions.put("SHIPPED", Arrays.asList("DELIVERED", "CANCELLED", "RETURNED"));
        allowedTransitions.put("DELIVERED", Arrays.asList("RECEIVED", "RETURNED"));
        
        List<String> finalStates = Arrays.asList("RECEIVED", "CANCELLED", "RETURNED");
        if (finalStates.contains(oldStatus)) {
            throw new RuntimeException("Đơn hàng đã kết thúc ở trạng thái " + oldStatus + ", không thể thay đổi.");
        }

        if (!newStatus.equals(oldStatus)) {
            List<String> allowed = allowedTransitions.get(oldStatus);
            if (allowed == null || !allowed.contains(newStatus)) {
                // If not in map, check if it's a forced move or log it
                logger.warn("Status transition from {} to {} might be restricted, but attempting to proceed.", oldStatus, newStatus);
            }
        }

        // Special logic for shipping
        if ("SHIPPING".equals(newStatus) || "SHIPPED".equals(newStatus)) {
            String method = request.getShippingMethod();
            String tracking = request.getTrackingNumber();
            
            if (method != null && !method.isBlank()) record.setShippingMethod(method);
            if (tracking != null && !tracking.isBlank()) record.setTrackingNumber(tracking);
            
            if (record.getShippingStartedAt() == null) {
                record.setShippingStartedAt(Instant.now());
            }
        }

        if ("DELIVERED".equals(newStatus) && record.getDeliveredAt() == null) {
            record.setDeliveredAt(Instant.now());
        }

        record.setStatus(newStatus);
        OrderRecord updatedRecord = orderRecordRepository.save(record);
        
        // Record History in sales database
        try {
            String trackingInfo = "";
            if (updatedRecord.getShippingMethod() != null && updatedRecord.getTrackingNumber() != null) {
                trackingInfo = " [" + updatedRecord.getShippingMethod() + ": " + updatedRecord.getTrackingNumber() + "]";
            }
            String note = (request.getNote() != null && !request.getNote().isEmpty()) ? request.getNote() : "Cập nhật qua Admin";
            
            jdbcTemplate.update(
                "INSERT INTO bizflow_sales_db.order_status_history (order_id, status, created_at, created_by, note) " +
                "VALUES (?, ?, NOW(), ?, ?)",
                id, updatedRecord.getStatus(), "ADMIN", note + trackingInfo
            );
        } catch (Exception e) {
            logger.warn("Failed to record status history for order {}", id, e.getMessage());
        }

        // Publish event to Kafka
        if (orderEventProducer != null) {
            try {
                PurchaseEvent event = new PurchaseEvent();
                event.setOrderId(updatedRecord.getId());
                event.setInvoiceNumber(updatedRecord.getInvoiceNumber());
                if (updatedRecord.getTotalAmount() != null) {
                    event.setTotalAmount(new java.math.BigDecimal(updatedRecord.getTotalAmount()));
                }
                event.setStatus(updatedRecord.getStatus());
                if (updatedRecord.getCreatedAt() != null) {
                    event.setCreatedAt(java.time.LocalDateTime.ofInstant(updatedRecord.getCreatedAt(), java.time.ZoneId.systemDefault()));
                } else {
                    event.setCreatedAt(java.time.LocalDateTime.now());
                }
                orderEventProducer.publishPurchaseEvent(event);
            } catch (Exception e) {
                logger.warn("Failed to publish Kafka event for order {}", id, e.getMessage());
            }
        }
        
        return toDto(updatedRecord);
    }

    private OrderSummaryDto toDto(OrderRecord record) {
        OrderSummaryDto dto = new OrderSummaryDto(
                record.getId(),
                record.getInvoiceNumber(),
                record.getStatus(),
                record.getCustomerName(),
                record.getTotalAmount(),
                record.getCreatedAt()
        );
        dto.setShippingMethod(record.getShippingMethod());
        dto.setTrackingNumber(record.getTrackingNumber());
        dto.setShippingStartedAt(record.getShippingStartedAt());
        dto.setDeliveredAt(record.getDeliveredAt());
        dto.setAddress(record.getAddress());
        return dto;
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        if (value instanceof byte[] bytes) {
            return bytes.length > 0 && bytes[0] != 0;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) return null;
        return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t");
    }

    private static java.time.Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp ts) return ts.toInstant();
        if (value instanceof java.util.Date d) return d.toInstant();
        if (value instanceof java.time.Instant i) return i;
        if (value instanceof java.time.OffsetDateTime odt) return odt.toInstant();
        if (value instanceof java.time.LocalDateTime ldt) {
            return ldt.toInstant(java.time.ZoneOffset.UTC);
        }
        return null;
    }
}
