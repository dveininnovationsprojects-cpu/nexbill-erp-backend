package com.example.billing_backend.service;

import com.example.billing_backend.dto.AuthRequest;
import com.example.billing_backend.dto.AuthResponse;
import com.example.billing_backend.dto.RegisterRequest;
import com.example.billing_backend.model.User;
import com.example.billing_backend.repository.UserRepository;
import com.example.billing_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.builder()
                    .message("User already register")
                    .token(null)
                    .build();
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .token(jwtToken)
                .build();
    }
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("Login successful") // Login message add panniyachu
                .token(jwtToken)
                .build();
    }
}