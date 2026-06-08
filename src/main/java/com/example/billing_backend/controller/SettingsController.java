package com.example.billing_backend.controller;

import com.example.billing_backend.dto.SettingsRequest;
import com.example.billing_backend.model.SystemSettings;
import com.example.billing_backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // =========================================================
    // 🔥 READ SETTINGS (Admin & Cashier Only)
    // Cashier needs this to fetch details (like Logo, GST) for Bill Printing
    // =========================================================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping
    public ResponseEntity<SystemSettings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    // =========================================================
    // 🔥 UPDATE SETTINGS (STRICTLY ADMIN ONLY)
    // =========================================================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<SystemSettings> updateSettings(
            @RequestBody SettingsRequest request,
            Principal principal) {

        // principal.getName() returns the logged-in Admin's email/username
        return ResponseEntity.ok(settingsService.updateSettings(request, principal.getName()));
    }

    // =========================================================
    // 🔥 RESET SETTINGS TO DEFAULT (STRICTLY ADMIN ONLY)
    // =========================================================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/reset")
    public ResponseEntity<SystemSettings> resetSettings(Principal principal) {

        return ResponseEntity.ok(settingsService.resetToDefault(principal.getName()));
    }
}