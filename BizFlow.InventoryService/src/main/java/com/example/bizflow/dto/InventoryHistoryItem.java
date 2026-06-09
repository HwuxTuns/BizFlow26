package com.example.bizflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class InventoryHistoryItem {

    private Long id;
    private Long productId;
    private String productName;
    private String type;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String note;
    private String createdAt;

    // Explicit constructor vì Lombok không hoạt động đúng trong môi trường này
    public InventoryHistoryItem(Long id, Long productId, String productName, String type,
            Integer quantity, BigDecimal unitPrice, String note, String createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.note = note;
        this.createdAt = createdAt;
    }
}
