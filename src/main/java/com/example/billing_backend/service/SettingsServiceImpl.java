package com.example.billing_backend.service;

import com.example.billing_backend.dto.SettingsRequest;
import com.example.billing_backend.model.SettingsAuditLog;
import com.example.billing_backend.model.SystemSettings;
import com.example.billing_backend.repository.SettingsAuditLogRepository;
import com.example.billing_backend.repository.SettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;
    private final SettingsAuditLogRepository auditLogRepository;

    // =========================================================
    // 🔥 Step 1: System Initialization (Generic Empty Defaults)
    // Namma company name hardcode panna koodadhu. Admin dhaan UI-la update pannanum!
    // =========================================================
    @PostConstruct
    public void initDefaultSettings() {
        if (settingsRepository.count() == 0) {
            SystemSettings defaultSettings = SystemSettings.builder()
                    .companyName("Please update Company Name") // Generic placeholder
                    .companyAddress("Please update Address")
                    .companyPhone("")
                    .companyEmail("")
                    .gstNumber("")
                    .invoicePrefix("INV")
                    .currency("INR")
                    .defaultReorderLevel(10)
                    .updatedAt(LocalDateTime.now())
                    .build();
            settingsRepository.save(defaultSettings);
        }
    }

    // =========================================================
    // 🔥 Step 2: Fetch Settings (Dynamic ID fetch)
    // =========================================================
    @Override
    public SystemSettings getSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("System settings not found!"));
    }

    // =========================================================
    // 🔥 Step 3: Admin Update Logic (From Frontend App)
    // =========================================================
    @Override
    @Transactional
    public SystemSettings updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings current = getSettings();

        // 🛡️ Rule 1: Valid GST Format Check (Check ONLY if Admin has entered a value)
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
        if (request.getGstNumber() != null && !request.getGstNumber().trim().isEmpty()) {
            if (!request.getGstNumber().matches(gstRegex)) {
                throw new RuntimeException("Invalid GST Number format!");
            }
            current.setGstNumber(request.getGstNumber().toUpperCase());
        } else {
            current.setGstNumber("");
        }

        // 🛡️ Rule 2: Invoice Prefix Check
        if (request.getInvoicePrefix() == null || request.getInvoicePrefix().trim().isEmpty()) {
            throw new RuntimeException("Invoice Prefix cannot be empty!");
        }

        // 🛡️ Rule 3: Low Stock Validations
        if (request.getDefaultReorderLevel() < 0) {
            throw new RuntimeException("Default Reorder Level cannot be negative!");
        }

        // 🔥 Dynamic Update: Override default values with Admin's input from Frontend
        current.setCompanyName(request.getCompanyName() != null && !request.getCompanyName().isEmpty() ? request.getCompanyName() : "Company Name Not Set");
        current.setTagline(request.getTagline());
        current.setCompanyEmail(request.getCompanyEmail());
        current.setCompanyPhone(request.getCompanyPhone());
        current.setWebsite(request.getWebsite());
        current.setCompanyAddress(request.getCompanyAddress());
        current.setCity(request.getCity());
        current.setState(request.getState());
        current.setPinCode(request.getPinCode());
        current.setPanNumber(request.getPanNumber());
        current.setCin(request.getCin());
        current.setLogoUrl(request.getLogoUrl());

        current.setInvoicePrefix(request.getInvoicePrefix().toUpperCase());
        current.setStartingNumber(request.getStartingNumber() != null ? request.getStartingNumber() : 1001L);
        current.setPaymentDueDays(request.getPaymentDueDays() != null ? request.getPaymentDueDays() : 7);
        current.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        current.setDateFormat(request.getDateFormat());
        current.setDefaultPaymentTerms(request.getDefaultPaymentTerms());
        current.setInvoiceFooterNote(request.getInvoiceFooterNote());

        current.setShowCompanyLogo(request.getShowCompanyLogo() != null ? request.getShowCompanyLogo() : true);
        current.setShowGstBreakdown(request.getShowGstBreakdown() != null ? request.getShowGstBreakdown() : true);
        current.setShowSignatureArea(request.getShowSignatureArea() != null ? request.getShowSignatureArea() : true);
        current.setShowPaymentQrCode(request.getShowPaymentQrCode() != null ? request.getShowPaymentQrCode() : false);
        current.setShowBankTransferDetails(request.getShowBankTransferDetails() != null ? request.getShowBankTransferDetails() : true);
        current.setShowTermsAndConditions(request.getShowTermsAndConditions() != null ? request.getShowTermsAndConditions() : true);

        current.setDefaultReorderLevel(request.getDefaultReorderLevel());
        current.setUpdatedAt(LocalDateTime.now());

        SystemSettings updatedSettings = settingsRepository.save(current);

        // 📜 Save Audit Log
        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary("Admin updated system settings via App")
                .build();
        auditLogRepository.save(log);

        return updatedSettings;
    }

    @Override
    @Transactional
    public SystemSettings resetToDefault(String updatedBy) {
        settingsRepository.deleteAll();
        initDefaultSettings(); // Recreate empty generic defaults

        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary("SYSTEM RESET TO GENERIC DEFAULTS")
                .build();
        auditLogRepository.save(log);

        return getSettings();
    }
}