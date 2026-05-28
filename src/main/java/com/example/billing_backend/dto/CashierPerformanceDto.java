package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CashierPerformanceDto {
    private String cashierName;
    private String cashierEmail;
    private Long billsGenerated;
    private BigDecimal totalRevenueHandled;
}