package com.example.billing_backend.dto;

import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDto {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String branch;
    private Role role;
    private Double basicSalary;
    private String counterNumber;
    private String shiftTiming;
    private UserStatus status;
}