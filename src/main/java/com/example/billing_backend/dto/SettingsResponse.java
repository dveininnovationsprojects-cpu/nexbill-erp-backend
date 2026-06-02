package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String gstNumber;
    private String invoicePrefix;
    private String currency;
    private int defaultReorderLevel;
    private String logoUrl;
    private LocalDateTime updatedAt;
}