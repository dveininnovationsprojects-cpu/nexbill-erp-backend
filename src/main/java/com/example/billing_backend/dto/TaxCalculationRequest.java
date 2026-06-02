package com.example.billing_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TaxCalculationRequest {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal gstPercentage;
}