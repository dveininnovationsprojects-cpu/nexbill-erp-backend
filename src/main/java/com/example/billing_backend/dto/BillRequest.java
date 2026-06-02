package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class BillRequest {
    private String paymentMethod;

    // 🔥 FIX: Frontend anuppura puthu Customer fields!
    private String customerName;
    private String customerPhone;
    private Long customerId; // Future use-kaga
}