package com.example.billing_backend.controller;

import com.example.billing_backend.dto.CartItemRequest;
import com.example.billing_backend.dto.CartSummaryResponse;
import com.example.billing_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String getCashierId(Principal principal) {
        return principal.getName();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PostMapping("/add")
    public ResponseEntity<CartSummaryResponse> addToCart(
            Principal principal,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCart(getCashierId(principal), request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PutMapping("/update")
    public ResponseEntity<CartSummaryResponse> updateQuantity(
            Principal principal,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(getCashierId(principal), request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartSummaryResponse> removeItem(
            Principal principal,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(getCashierId(principal), productId));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping
    public ResponseEntity<CartSummaryResponse> viewCart(Principal principal) {
        return ResponseEntity.ok(cartService.viewCart(getCashierId(principal)));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Principal principal) {
        cartService.clearCart(getCashierId(principal));
        return ResponseEntity.ok("Cart cleared successfully!");
    }
}