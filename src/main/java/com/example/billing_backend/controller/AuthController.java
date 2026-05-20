package com.example.billing_backend.controller;

import com.example.billing_backend.dto.AuthRequest;
import com.example.billing_backend.dto.AuthResponse;
import com.example.billing_backend.dto.RegisterRequest;
import com.example.billing_backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/register") // Initial ah admin / cashier add panna
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login") // Token generate panni Cookie la set panna
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        // 1. Service la irunthu token and message ah vaangurathu
        AuthResponse authResponse = service.authenticate(request);

        // 2. HRM backend maari HttpOnly Cookie create panrathu
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)       // JavaScript aala intha cookie ah access panna mudiyathu (XSS safe)
                .secure(false)      // AWS Production-la HTTPS use pannum pothu itha 'true' nu mathikanum
                .path("/")          // Ella API routes kum intha cookie work aagum
                .maxAge(24 * 60 * 60) // 1 Day (24 hours) validity
                .build();

        // 3. Response-la Cookie header and JSON body rendu me sethu anuppurathu
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }
}