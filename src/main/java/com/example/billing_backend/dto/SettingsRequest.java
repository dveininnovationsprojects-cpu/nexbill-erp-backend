package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class SettingsRequest {
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String gstNumber;
    private String invoicePrefix;
    private String currency;
    private int defaultReorderLevel;
    private String logoUrl;
}