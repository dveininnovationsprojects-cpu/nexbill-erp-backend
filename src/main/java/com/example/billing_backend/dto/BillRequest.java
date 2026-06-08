package com.example.billing_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillRequest {
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private Integer customerId;
    private BigDecimal discountAmount;
}