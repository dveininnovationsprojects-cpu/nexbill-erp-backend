package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockAlertResponse {
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal availableQuantity;
    private BigDecimal reorderLevel;
    private String status; // "SAFE", "LOW STOCK", "CRITICAL STOCK", "OUT OF STOCK"
    private LocalDateTime updatedAt; // 🔥 Enterprise Fix: Last Updated Time
}