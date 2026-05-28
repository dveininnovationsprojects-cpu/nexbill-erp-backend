package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class CategoryValuationDto {
    private String categoryName;
    private BigDecimal totalValuation;
}

