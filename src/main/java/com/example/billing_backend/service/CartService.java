package com.example.billing_backend.service;

import com.example.billing_backend.dto.CartItemRequest;
import com.example.billing_backend.dto.CartSummaryResponse;

public interface CartService {
    CartSummaryResponse addToCart(String cashierId, CartItemRequest request);
    CartSummaryResponse updateQuantity(String cashierId, CartItemRequest request);
    CartSummaryResponse removeItem(String cashierId, Long productId);
    CartSummaryResponse viewCart(String cashierId);
    void clearCart(String cashierId);
}