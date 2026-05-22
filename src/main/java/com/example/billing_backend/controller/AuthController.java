package com.example.billing_backend.controller;

import com.example.billing_backend.dto.AuthRequest;
import com.example.billing_backend.dto.AuthResponse;
import com.example.billing_backend.dto.RegisterRequest;
import com.example.billing_backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        authResponse.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {

        ResponseCookie cleanCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(AuthResponse.builder()
                        .message("Logged out successfully")
                        .token(null)
                        .build());
    }
        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "jwt", required = false) String refreshToken) {
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new RuntimeException("Refresh Token Cookie is missing");
            }
            return ResponseEntity.ok(service.refreshToken(refreshToken));
        }
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(service.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody com.example.billing_backend.dto.PasswordResetDto request) {
        return ResponseEntity.ok(service.resetPassword(request));
    }
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        // user service function-ah call pannunga
        service.deleteUser(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

}