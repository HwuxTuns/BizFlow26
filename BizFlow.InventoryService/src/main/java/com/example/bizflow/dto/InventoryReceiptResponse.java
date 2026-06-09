package com.example.bizflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReceiptResponse {

    private Long productId;
    private Integer newStock;
    private Long transactionId;

    // Explicit constructor for compatibility
    public InventoryReceiptResponse(Long productId, int newStock, Long transactionId) {
        this.productId = productId;
        this.newStock = newStock;
        this.transactionId = transactionId;
    }
}
