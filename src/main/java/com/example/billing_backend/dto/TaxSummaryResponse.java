package com.example.billing_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 🔥 Magic: Hides fields if they are null!
public class TaxSummaryResponse {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal gstPercentage; // Will be hidden for Bill Summary
    private BigDecimal gstAmount;
    private BigDecimal grandTotal;
}