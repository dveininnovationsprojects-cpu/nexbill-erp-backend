package com.example.billing_backend.service;

import com.example.billing_backend.model.SystemSettings;
import com.example.billing_backend.dto.SettingsRequest;

public interface SettingsService {
    SystemSettings getSettings();
    SystemSettings updateSettings(SettingsRequest request, String updatedBy);
    SystemSettings resetToDefault(String updatedBy);
}