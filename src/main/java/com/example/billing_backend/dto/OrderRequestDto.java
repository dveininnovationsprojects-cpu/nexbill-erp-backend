package com.example.billing_backend.dto;

import com.example.billing_backend.model.PaymentMode;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDto {
    private Integer customerId; // Optional (Send null for walk-in)
    private PaymentMode paymentMode;
    private BigDecimal discountAmount;
    private List<OrderItemRequestDto> items;
    private String externalTransactionRef;
}