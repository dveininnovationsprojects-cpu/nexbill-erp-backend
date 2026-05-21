package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private Long inventoryId;
    private String productName;
    private Integer availableQuantity;
    private Integer reorderLevel;
    private String status; // 'OK' or 'LOW STOCK' nu frontend-la color-ah kaata use aagum
}