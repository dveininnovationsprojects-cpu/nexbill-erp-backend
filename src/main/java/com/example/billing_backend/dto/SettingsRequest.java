package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class SettingsRequest {
    private String companyName;
    private String tagline;
    private String companyEmail;
    private String companyPhone;
    private String website;
    private String companyAddress;
    private String city;
    private String state;
    private String pinCode;
    private String gstNumber;
    private String panNumber;
    private String cin;
    private String logoUrl;

    private String invoicePrefix;
    private Long startingNumber;
    private Integer paymentDueDays;
    private String currency;
    private String dateFormat;
    private String defaultPaymentTerms;
    private String invoiceFooterNote;

    private Boolean showCompanyLogo;
    private Boolean showGstBreakdown;
    private Boolean showSignatureArea;
    private Boolean showPaymentQrCode;
    private Boolean showBankTransferDetails;
    private Boolean showTermsAndConditions;

    private int defaultReorderLevel;
}