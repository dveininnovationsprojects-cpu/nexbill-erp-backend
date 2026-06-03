package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {
    private BigDecimal totalGrossRevenue;
    private Long totalInvoicesGenerated;
    private BigDecimal totalTaxCollected;
    private BigDecimal totalDiscountsGiven;
    private List<PaymentModeBreakdownDto> paymentBreakdown;
    private List<TopSellingProductDto> topProducts;
    private List<CashierPerformanceDto> cashierPerformances;
}