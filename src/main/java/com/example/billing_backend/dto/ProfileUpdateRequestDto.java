package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDto {
    private String name;
    private String email;
    private String phone;
    private String password; // Optional - Puthu password matha nenaicha mattum pass pannuvanga
}