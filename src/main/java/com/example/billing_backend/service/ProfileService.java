package com.example.billing_backend.service;

import com.example.billing_backend.dto.AdminUserUpdateDto;
import com.example.billing_backend.dto.ProfileResponseDto;
import com.example.billing_backend.dto.ProfileUpdateRequestDto;

public interface ProfileService {
    ProfileResponseDto getMyProfile(String email);
    ProfileResponseDto updateMyProfile(String currentEmail, ProfileUpdateRequestDto request);
    ProfileResponseDto updateStaffByAdmin(Integer staffId, AdminUserUpdateDto request);
}