package com.example.billing_backend.dto;

import com.example.billing_backend.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentModeBreakdownDto {
    private PaymentMode paymentMode;
    private BigDecimal totalAmount;
    private Long transactionCount;
}