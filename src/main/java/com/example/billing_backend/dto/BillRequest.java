package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class BillRequest {
    private String paymentMethod; // e.g., "CASH", "UPI", "CARD"
}