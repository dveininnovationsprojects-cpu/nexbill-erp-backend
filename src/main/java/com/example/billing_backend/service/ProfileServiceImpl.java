package com.example.billing_backend.service;

import com.example.billing_backend.dto.AdminUserUpdateDto;
import com.example.billing_backend.dto.ProfileResponseDto;
import com.example.billing_backend.dto.ProfileUpdateRequestDto;
import com.example.billing_backend.model.User;
import com.example.billing_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileResponseDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found!"));
        return mapToProfileResponse(user);
    }

    @Override
    public ProfileResponseDto updateMyProfile(String currentEmail, ProfileUpdateRequestDto request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Profile not found!"));

        // SMART CHECK: Email update panra maari irundha, athu already vera yaarachum use pandrangala nu paakanum
        if (request.getEmail() != null && !request.getEmail().equals(currentEmail)) {
            Optional<User> existingUserWithNewEmail = userRepository.findByEmail(request.getEmail());
            if (existingUserWithNewEmail.isPresent()) {
                throw new IllegalArgumentException("Email address is already in use by another account!");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    @Override
    public ProfileResponseDto updateStaffByAdmin(Integer staffId, AdminUserUpdateDto request) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff account not found!"));

        if (request.getBasicSalary() != null) staff.setBasicSalary(request.getBasicSalary());
        if (request.getCounterNumber() != null) staff.setCounterNumber(request.getCounterNumber());
        if (request.getShiftTiming() != null) staff.setShiftTiming(request.getShiftTiming());
        if (request.getStatus() != null) staff.setStatus(request.getStatus());

        User updatedStaff = userRepository.save(staff);
        return mapToProfileResponse(updatedStaff);
    }

    // Helper mapping function target layout bypass logic setup
    private ProfileResponseDto mapToProfileResponse(User user) {
        return ProfileResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .branch(user.getBranch())
                .role(user.getRole())
                .basicSalary(user.getBasicSalary())
                .counterNumber(user.getCounterNumber())
                .shiftTiming(user.getShiftTiming())
                .status(user.getStatus())
                .build();
    }
}