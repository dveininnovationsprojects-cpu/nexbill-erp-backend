package com.example.billing_backend.service;

import com.example.billing_backend.dto.AuthRequest;
import com.example.billing_backend.dto.AuthResponse;
import com.example.billing_backend.dto.RegisterRequest;
import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.model.UserStatus;
import com.example.billing_backend.repository.NotificationRepository;
import com.example.billing_backend.repository.UserRepository;
import com.example.billing_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.builder()
                    .message("User already registered")
                    .token(null)
                    .build();
        }

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

        if (request.getRole() == Role.CASHIER) {
            var notification = Notification.builder()
                    .message("New Cashier Registration Pending: " + user.getName())
                    .relatedUserEmail(user.getEmail())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

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
        var user = repository.findByEmail(request.getEmail()).orElseThrow();

        if (user.getStatus() != UserStatus.ACTIVE) {
            return AuthResponse.builder()
                    .message("Account is not active. Please wait for Admin approval.")
                    .token(null)
                    .build();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("Login successful")
                .token(jwtToken)
                .build();
    }
}