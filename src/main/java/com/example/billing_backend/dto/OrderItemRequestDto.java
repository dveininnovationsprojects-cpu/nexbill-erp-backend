package com.example.billing_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequestDto {
    private Long productId;
    private BigDecimal quantity;
}