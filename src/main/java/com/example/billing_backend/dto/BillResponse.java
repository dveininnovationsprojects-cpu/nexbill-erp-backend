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
    private BigDecimal grandTotal;
    private String paymentMethod;
    private String message;
    private LocalDateTime timestamp;
}