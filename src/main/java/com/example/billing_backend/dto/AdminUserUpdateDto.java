package com.example.billing_backend.dto;

import com.example.billing_backend.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserUpdateDto {
    private Double basicSalary;
    private String counterNumber;
    private String shiftTiming;
    private UserStatus status;
    private String branch;
}