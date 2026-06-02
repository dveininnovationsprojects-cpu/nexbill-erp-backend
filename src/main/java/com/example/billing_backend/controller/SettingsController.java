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

    // ALL Roles can VIEW settings (Needed for UI display, PDF gen, etc.)
    @GetMapping
    public ResponseEntity<SystemSettings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    // 🔥 Rule 1: ONLY ADMIN CAN UPDATE
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<SystemSettings> updateSettings(
            @RequestBody SettingsRequest request,
            Principal principal) {
        // principal.getName() returns the logged-in user's email/username
        return ResponseEntity.ok(settingsService.updateSettings(request, principal.getName()));
    }

    // 🔥 Rule 1: ONLY ADMIN CAN RESET
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/reset")
    public ResponseEntity<SystemSettings> resetSettings(Principal principal) {
        return ResponseEntity.ok(settingsService.resetToDefault(principal.getName()));
    }
}