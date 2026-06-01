package com.example.billing_backend.service;

import com.example.billing_backend.dto.AuthRequest;
import com.example.billing_backend.dto.AuthResponse;
import com.example.billing_backend.dto.RegisterRequest;
import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.model.UserStatus;
import com.example.billing_backend.model.RefreshToken;
import com.example.billing_backend.repository.UserRepository;
import com.example.billing_backend.repository.RefreshTokenRepository;
import com.example.billing_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    // Add this line at the top parameter definitions inside your service classes
    private final NotificationService notificationService;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.builder()
                    .message("User already registered")
                    .token(null)
                    .build();
        }

        validatePasswordStrength(request.getPassword());

        UserStatus initialStatus = UserStatus.PENDING;

        if (request.getRole() == Role.ADMIN) {
            if (request.getAdminSecretKey() == null || !request.getAdminSecretKey().equals("DVEIN_SUPER_SECRET_KEY_123")) {
                return AuthResponse.builder()
                        .message("Invalid admin secret key")
                        .token(null)
                        .build();
            }
            initialStatus = UserStatus.ACTIVE;
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(initialStatus)
                .build();
        repository.save(user);

        if (user.getRole() == Role.CASHIER) {
            notificationService.triggerNewRegistrationAlertToAdmins(user);
        }
        if (request.getRole() == Role.CASHIER) {
            return AuthResponse.builder()
                    .message("Registration successful. Waiting for Admin approval.")
                    .token(null)
                    .build();
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .message("Admin Registration successful")
                .token(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            var user = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

            if (user.getStatus() == UserStatus.PENDING) {
                return AuthResponse.builder()
                        .message("Account is not active. Please wait for Admin approval.")
                        .token(null)
                        .build();
            } else if (user.getStatus() == UserStatus.SUSPENDED) {
                return AuthResponse.builder()
                        .message("You are suspended! Please contact the administrator to restore access.")
                        .token(null)
                        .build();
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            var jwtToken = jwtService.generateToken(user);
            var refreshTokenString = jwtService.generateRefreshToken(user);

            refreshTokenRepository.deleteByUser(user);

            var refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenString)
                    .expiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7))
                    .build();
            refreshTokenRepository.save(refreshToken);

            return AuthResponse.builder()
                    .message("Login successful")
                    .token(jwtToken)
                    .refreshToken(refreshTokenString)
                    .build();

        } catch (org.springframework.security.core.AuthenticationException | java.util.NoSuchElementException e) {
            return AuthResponse.builder()
                    .message("Invalid email or password")
                    .token(null)
                    .build();
        }
    }

    public AuthResponse refreshToken(String requestRefreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token is missing or invalid!"));

        if (tokenEntity.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        var user = tokenEntity.getUser();
        var accessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("Token refreshed successfully")
                .token(accessToken)
                .build();
    }
    public AuthResponse forgotPassword(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User account entry not found!"));

        java.util.Random random = new java.util.Random();
        String otp = String.format("%06d", random.nextInt(1000000));

        user.setResetOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        repository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return AuthResponse.builder()
                .message("Verification code sent successfully")
                .token(null)
                .build();
    }

    public AuthResponse resetPassword(com.example.billing_backend.dto.PasswordResetDto request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User account not found!"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid verification credentials!");
        }

        if (user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            user.setResetOtp(null);
            user.setOtpExpiry(null);
            repository.save(user);
            throw new RuntimeException("Verification code expired!");
        }
        validatePasswordStrength(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        repository.save(user);

        return AuthResponse.builder()
                .message("Password updated successfully")
                .token(null)
                .build();
    }
    public void deleteUser(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("account not found!"));

        if (refreshTokenRepository != null) {
            refreshTokenRepository.deleteByUser(user);
        }


        repository.delete(user);
    }
    // 🔥 CENTRALIZED PASSWORD VALIDATOR GATEWAY
    private void validatePasswordStrength(String password) {
        if (password == null) {
            throw new RuntimeException("Password cannot be empty!");
        }

        // Regex for: 1 Uppercase, 1 Digit, 1 Special Character, Min 8 Length
        String regex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]).{8,}$";

        if (!password.matches(regex)) {
            throw new RuntimeException("Password is too weak! It must be at least 8 characters long, contain at least one uppercase letter, one number, and one special character.");
        }
    }
}