package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopSellingProductDto {
    private String productName;
    private BigDecimal totalQuantitySold;
    private BigDecimal totalRevenueGenerated;
}