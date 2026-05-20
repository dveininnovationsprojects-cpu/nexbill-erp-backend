package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashierApprovalRequest {
    private String phone;
    private String branch;
    private String counterNumber;
    private String shiftTiming;
    private Double basicSalary;
}