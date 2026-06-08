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

    @PostConstruct
    public void initDefaultSettings() {
        if (settingsRepository.count() == 0) {
            SystemSettings defaultSettings = SystemSettings.builder()
                    .companyName("Please update Company Name")
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

    @Override
    public SystemSettings getSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("System settings not found!"));
    }

    @Override
    @Transactional
    public SystemSettings updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings current = getSettings();

        // 🛡️ Rule 1: Valid GST Format Check
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
        if (request.getGstNumber() != null && !request.getGstNumber().trim().isEmpty()) {
            if (!request.getGstNumber().matches(gstRegex)) {
                throw new RuntimeException("Invalid GST Number format!");
            }
            current.setGstNumber(request.getGstNumber().toUpperCase());
        }

        // 🛡️ Rule 2: Invoice Prefix Check
        if (request.getInvoicePrefix() != null && !request.getInvoicePrefix().trim().isEmpty()) {
            current.setInvoicePrefix(request.getInvoicePrefix().toUpperCase());
        }

        // 🛡️ Rule 3: Low Stock Validations
        if (request.getDefaultReorderLevel() >= 0) {
            current.setDefaultReorderLevel(request.getDefaultReorderLevel());
        }

        // 🔥 STRICT SAVE LOGIC: Frontend data anuppina mattum update pannu, illana pazhayadha kaapaathu!
        current.setCompanyName(request.getCompanyName() != null && !request.getCompanyName().isEmpty() ? request.getCompanyName() : current.getCompanyName());
        current.setTagline(request.getTagline() != null ? request.getTagline() : current.getTagline());
        current.setCompanyEmail(request.getCompanyEmail() != null ? request.getCompanyEmail() : current.getCompanyEmail());
        current.setCompanyPhone(request.getCompanyPhone() != null ? request.getCompanyPhone() : current.getCompanyPhone());
        current.setWebsite(request.getWebsite() != null ? request.getWebsite() : current.getWebsite());
        current.setCompanyAddress(request.getCompanyAddress() != null ? request.getCompanyAddress() : current.getCompanyAddress());
        current.setCity(request.getCity() != null ? request.getCity() : current.getCity());
        current.setState(request.getState() != null ? request.getState() : current.getState());
        current.setPinCode(request.getPinCode() != null ? request.getPinCode() : current.getPinCode());
        current.setPanNumber(request.getPanNumber() != null ? request.getPanNumber() : current.getPanNumber());
        current.setCin(request.getCin() != null ? request.getCin() : current.getCin());

        // ========================================================
        // 🚀 LOGO STRICT SAVE FIX
        // Frontend pudhu logo URL anupina mattum thaan DB-la ulla povanum.
        // Null-ah vandha pazhaya logo apdiye safe-ah irukkum!
        // ========================================================
        if (request.getLogoUrl() != null && !request.getLogoUrl().trim().isEmpty()) {
            current.setLogoUrl(request.getLogoUrl());
        }

        // Numbers & Settings
        current.setStartingNumber(request.getStartingNumber() != null ? request.getStartingNumber() : current.getStartingNumber());
        current.setPaymentDueDays(request.getPaymentDueDays() != null ? request.getPaymentDueDays() : current.getPaymentDueDays());
        current.setCurrency(request.getCurrency() != null ? request.getCurrency() : current.getCurrency());
        current.setDateFormat(request.getDateFormat() != null ? request.getDateFormat() : current.getDateFormat());
        current.setDefaultPaymentTerms(request.getDefaultPaymentTerms() != null ? request.getDefaultPaymentTerms() : current.getDefaultPaymentTerms());
        current.setInvoiceFooterNote(request.getInvoiceFooterNote() != null ? request.getInvoiceFooterNote() : current.getInvoiceFooterNote());

        // Toggles
        current.setShowCompanyLogo(request.getShowCompanyLogo() != null ? request.getShowCompanyLogo() : current.getShowCompanyLogo());
        current.setShowGstBreakdown(request.getShowGstBreakdown() != null ? request.getShowGstBreakdown() : current.getShowGstBreakdown());
        current.setShowSignatureArea(request.getShowSignatureArea() != null ? request.getShowSignatureArea() : current.getShowSignatureArea());
        current.setShowPaymentQrCode(request.getShowPaymentQrCode() != null ? request.getShowPaymentQrCode() : current.getShowPaymentQrCode());
        current.setShowBankTransferDetails(request.getShowBankTransferDetails() != null ? request.getShowBankTransferDetails() : current.getShowBankTransferDetails());
        current.setShowTermsAndConditions(request.getShowTermsAndConditions() != null ? request.getShowTermsAndConditions() : current.getShowTermsAndConditions());

        current.setUpdatedAt(LocalDateTime.now());

        SystemSettings updatedSettings = settingsRepository.save(current);

        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary("Admin updated system settings (Strict Save Applied)")
                .build();
        auditLogRepository.save(log);

        return updatedSettings;
    }

    @Override
    @Transactional
    public SystemSettings resetToDefault(String updatedBy) {
        settingsRepository.deleteAll();
        initDefaultSettings();

        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary("SYSTEM RESET TO GENERIC DEFAULTS")
                .build();
        auditLogRepository.save(log);

        return getSettings();
    }
}