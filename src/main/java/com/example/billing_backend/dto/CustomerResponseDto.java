package com.example.billing_backend.dto;

import com.example.billing_backend.model.CustomerTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDto {
    private Integer id;
    private String name;
    private String mobile;
    private String email;
    private CustomerTier tier;
    private Double totalSpentAmount;
    private Double creditLimit;
    private Double outstandingDebt;
    private com.example.billing_backend.model.CustomerStatus status;
    private java.time.LocalDateTime lastCreditDateTime;
}