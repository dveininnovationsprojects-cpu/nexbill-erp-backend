package com.example.billing_backend.controller;

import com.example.billing_backend.dto.AdminUserUpdateDto;
import com.example.billing_backend.dto.ProfileResponseDto;
import com.example.billing_backend.dto.ProfileUpdateRequestDto;
import com.example.billing_backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 1. Current user avanga profile-ah view panrathuku
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ProfileResponseDto> getMyProfile(Principal principal) {
        // Principal extracts the username (email) directly from the valid JWT Token
        return ResponseEntity.ok(profileService.getMyProfile(principal.getName()));
    }

    // 2. Current user avanga own details-ah mathikurathuku (Email, Name, Password)
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            Principal principal,
            @RequestBody ProfileUpdateRequestDto request) {
        return ResponseEntity.ok(profileService.updateMyProfile(principal.getName(), request));
    }

    // 3. Admin mattum matha staff-oda salary, timing and status-ah mathurathuku
    @PutMapping("/admin/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponseDto> updateStaffDetailsByAdmin(
            @PathVariable Integer id,
            @RequestBody AdminUserUpdateDto request) {
        return ResponseEntity.ok(profileService.updateStaffByAdmin(id, request));
    }
}