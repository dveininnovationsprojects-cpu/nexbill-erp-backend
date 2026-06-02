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

    // 🔥 Step 1: System Initialization
    @PostConstruct
    public void initDefaultSettings() {
        if (!settingsRepository.existsById(1L)) {
            SystemSettings defaultSettings = SystemSettings.builder()
                    .id(1L)
                    .companyName("DVein Innovation Pvt Ltd")
                    .companyAddress("Karapakkam, Chennai")
                    .companyPhone("+91 98765 43210")
                    .companyEmail("admin@dvein.com")
                    .gstNumber("33ABCDE1234F1Z5")
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
        return settingsRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("System settings not found!"));
    }

    @Override
    @Transactional
    public SystemSettings updateSettings(SettingsRequest request, String updatedBy) {
        SystemSettings current = getSettings();

        // 🔥 Rule 2: Valid GST Format Check (Standard Indian GSTIN Regex)
        String gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
        if (request.getGstNumber() != null && !request.getGstNumber().matches(gstRegex)) {
            throw new RuntimeException("Invalid GST Number format!");
        }

        // 🔥 Rule 3: Invoice Prefix Check
        if (request.getInvoicePrefix() == null || request.getInvoicePrefix().trim().isEmpty()) {
            throw new RuntimeException("Invoice Prefix cannot be empty!");
        }

        // 🔥 Rule 4: Low Stock Validations
        if (request.getDefaultReorderLevel() < 0) {
            throw new RuntimeException("Default Reorder Level cannot be negative!");
        }

        // 🔥 Rule 5: Generating Audit Log string
        StringBuilder changes = new StringBuilder("Updated: ");
        if (!current.getCompanyName().equals(request.getCompanyName())) changes.append("Company Name, ");
        if (!current.getGstNumber().equals(request.getGstNumber())) changes.append("GST Number, ");
        if (current.getDefaultReorderLevel() != request.getDefaultReorderLevel()) changes.append("Reorder Level, ");

        // Update values
        current.setCompanyName(request.getCompanyName());
        current.setCompanyAddress(request.getCompanyAddress());
        current.setCompanyPhone(request.getCompanyPhone());
        current.setCompanyEmail(request.getCompanyEmail());
        current.setGstNumber(request.getGstNumber().toUpperCase());
        current.setInvoicePrefix(request.getInvoicePrefix().toUpperCase());
        current.setCurrency(request.getCurrency());
        current.setDefaultReorderLevel(request.getDefaultReorderLevel());
        current.setLogoUrl(request.getLogoUrl());
        current.setUpdatedAt(LocalDateTime.now());

        SystemSettings updatedSettings = settingsRepository.save(current);

        // Save Audit Log
        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary(changes.toString().endsWith(", ") ? changes.substring(0, changes.length() - 2) : "Minor fixes")
                .build();
        auditLogRepository.save(log);

        return updatedSettings;
    }

    @Override
    @Transactional
    public SystemSettings resetToDefault(String updatedBy) {
        settingsRepository.deleteAll();
        initDefaultSettings(); // Recreate defaults

        SettingsAuditLog log = SettingsAuditLog.builder()
                .updatedBy(updatedBy)
                .changesSummary("SYSTEM RESET TO DEFAULTS")
                .build();
        auditLogRepository.save(log);

        return getSettings();
    }
}