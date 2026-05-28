package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdvancedDashboardDto {
    // 1. Financial KPIs
    private BigDecimal grossRevenue;
    private BigDecimal netProfitMargin;
    private BigDecimal averageTicketSize;
    private BigDecimal totalDiscountsGiven;

    // 2. Inventory & Stock
    private Long outOfStockCount;
    private List<CategoryValuationDto> categoryValuations;
    private Long deadStockCount; // Items not sold in last 90 days

    // 3. Cashier Audits
    private List<CashierPerformanceDto> topCashiers;

    // 4. Customer Insights
    private List<CustomerInsightDto> topSpenders;
    private Long newCustomersAcquired;

    // 5. Product Analytics
    private List<TopProductDto> fastMovingProducts;
    private List<TopProductDto> highRevenueProducts;
}