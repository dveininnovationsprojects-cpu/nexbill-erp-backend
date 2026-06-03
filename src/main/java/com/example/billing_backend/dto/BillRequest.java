package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class BillRequest {
    private String paymentMethod;

    // Frontend anuppura Customer details
    private String customerName;
    private String customerPhone;

    // 🔥 IDHU THAAN MISSING! Indha line add aanadhum error parandhidum.
    private String customerEmail;

    private Long customerId; // Future use-kaga
}