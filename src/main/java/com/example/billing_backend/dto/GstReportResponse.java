package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class GstReportResponse {
    private long totalBillsGenerated;
    private BigDecimal totalSalesSubtotal;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalGstCollected;
    private BigDecimal totalRevenue; // Grand Total sum
}