package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;


@Data @AllArgsConstructor
public class CustomerInsightDto {
    private String name;
    private String mobile;
    private Double totalSpent;
}

