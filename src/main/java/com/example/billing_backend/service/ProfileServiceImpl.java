package com.example.billing_backend.service;

import com.example.billing_backend.dto.AdminUserUpdateDto;
import com.example.billing_backend.dto.ProfileResponseDto;
import com.example.billing_backend.dto.ProfileUpdateRequestDto;
import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.NotificationType;
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
    // Add this line at the top parameter definitions inside your service classes
    private final NotificationService notificationService;

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
            validatePasswordStrength(request.getPassword());

            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    @Override
    public ProfileResponseDto updateStaffByAdmin(Integer staffId, AdminUserUpdateDto request) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff account not found!"));

        boolean detailsChanged = false;

        if (request.getBranch() != null && !request.getBranch().trim().isEmpty()) {
            staff.setBranch(request.getBranch());
            detailsChanged = true;
        }

        if (request.getBasicSalary() != null) {
            staff.setBasicSalary(request.getBasicSalary());
            detailsChanged = true;
        }
        if (request.getCounterNumber() != null) {
            staff.setCounterNumber(request.getCounterNumber());
            detailsChanged = true;
        }
        if (request.getShiftTiming() != null) {
            staff.setShiftTiming(request.getShiftTiming());
            detailsChanged = true;
        }

        // Check if status is transitioning to ACTIVE status constraints limits
        boolean statusChangedToActive = request.getStatus() != null
                && request.getStatus() == com.example.billing_backend.model.UserStatus.ACTIVE
                && staff.getStatus() != com.example.billing_backend.model.UserStatus.ACTIVE;

        if (request.getStatus() != null) staff.setStatus(request.getStatus());

        User updatedStaff = userRepository.save(staff);

        if (detailsChanged) {
            // Create a specific alert for this user
            Notification alert = Notification.builder()
                    .title("Profile Updated")
                    .message("Admin updated your details (phone, shift, salary). Please review.")
                    .type(NotificationType.SYSTEM_UPDATE) // Or create a new enum like PROFILE_DETAILS_UPDATED
                    .targetedUser(updatedStaff) // Targeting ONLY this specific cashier
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            // Assuming you have notificationRepository injected here, or call a method in NotificationService
            // notificationRepository.save(alert);
            notificationService.sendDirectNotification(updatedStaff, "Profile Updated", "Admin updated your details.");
        }

        // ==========================================
        // 🚀 TRIGGER NOTIFICATION: ADMIN PROFILE APPROVAL SPRINT
        // ==========================================
        if (statusChangedToActive) {
            notificationService.triggerApprovalAlertToCashier(updatedStaff);
        }
        // ==========================================

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
    private void validatePasswordStrength(String password) {
        if (password == null) return;
        String regex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]).{8,}$";
        if (!password.matches(regex)) {
            throw new RuntimeException("Password must be at least 8 characters long, containing 1 uppercase letter, 1 number, and 1 special character.");
        }
    }
}