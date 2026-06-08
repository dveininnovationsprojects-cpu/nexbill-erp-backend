package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

    // 🔥 Fix: Increased column size for Base64 Logo strings
    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    // 🔥 INVOICE SETTINGS
    private String invoicePrefix;
    private Long startingNumber;
    private Integer paymentDueDays;
    private String currency;
    private String dateFormat;

    @Column(columnDefinition = "TEXT")
    private String defaultPaymentTerms;

    @Column(columnDefinition = "TEXT")
    private String invoiceFooterNote;

    // 🔥 DISPLAY OPTIONS (TOGGLES)
    private Boolean showCompanyLogo;
    private Boolean showGstBreakdown;
    private Boolean showSignatureArea;
    private Boolean showPaymentQrCode;
    private Boolean showBankTransferDetails;
    private Boolean showTermsAndConditions;

    private int defaultReorderLevel;
    private LocalDateTime updatedAt;
}