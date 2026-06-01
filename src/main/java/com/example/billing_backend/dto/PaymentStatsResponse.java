package com.example.billing_backend.dto;

import com.example.billing_backend.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {
    private PaymentMode paymentMode;
    private BigDecimal totalAmountCollected;
    private Long transactionCount;
}