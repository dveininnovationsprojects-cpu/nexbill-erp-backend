package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class TopProductDto {
    private String productName;
    private BigDecimal metricValue;
}
