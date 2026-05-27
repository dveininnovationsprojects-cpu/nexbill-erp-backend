package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {
    private Long inventoryId;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal availableQuantity; // 🔥 FIX: BigDecimal Use panniyachu
    private BigDecimal reorderLevel;      // 🔥 FIX: BigDecimal Use panniyachu
    private LocalDateTime updatedAt;
}