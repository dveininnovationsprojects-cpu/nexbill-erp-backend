package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillResponse {
    private String invoiceNumber;
    private String cashierId;

    // 🔥 FIX: Response-la customer name add panniyachi!
    private String customerName;

    private BigDecimal grandTotal;
    private String paymentMethod;
    private String message;
    private LocalDateTime timestamp;
    private String status;
}