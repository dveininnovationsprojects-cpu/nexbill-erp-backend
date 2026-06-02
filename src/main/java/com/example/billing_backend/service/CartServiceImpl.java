package com.example.billing_backend.service;

import com.example.billing_backend.dto.CartItemRequest;
import com.example.billing_backend.dto.CartItemResponse;
import com.example.billing_backend.dto.CartSummaryResponse;
import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.model.Product;
import com.example.billing_backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final InventoryRepository inventoryRepository;
    private final Map<String, List<CartItemResponse>> cartStore = new ConcurrentHashMap<>();

    @Override
    public CartSummaryResponse addToCart(String cashierId, CartItemRequest request) {
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Inventory inv = inventoryRepository.findByProduct_Id(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        Product product = inv.getProduct();
        if (product.isDeleted()) {
            throw new RuntimeException("Cannot add deleted product to cart");
        }

        List<CartItemResponse> cashierCart = cartStore.computeIfAbsent(cashierId, k -> new ArrayList<>());
        Optional<CartItemResponse> existingItemOpt = cashierCart.stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst();

        BigDecimal requestedQty = request.getQuantity();

        if (existingItemOpt.isPresent()) {
            CartItemResponse existingItem = existingItemOpt.get();
            BigDecimal newQty = existingItem.getQuantity().add(requestedQty);

            if (inv.getAvailableQuantity().compareTo(newQty) < 0) {
                throw new RuntimeException("Insufficient Stock! Only " + inv.getAvailableQuantity() + " left.");
            }
            updateItemCalculations(existingItem, newQty, product);
        } else {
            if (inv.getAvailableQuantity().compareTo(requestedQty) < 0) {
                throw new RuntimeException("Insufficient Stock! Only " + inv.getAvailableQuantity() + " left.");
            }

            CartItemResponse newItem = CartItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .sku(product.getSku())
                    .build();

            updateItemCalculations(newItem, requestedQty, product);
            cashierCart.add(newItem);
        }
        return generateCartSummary(cashierCart);
    }

    @Override
    public CartSummaryResponse updateQuantity(String cashierId, CartItemRequest request) {
        List<CartItemResponse> cashierCart = cartStore.getOrDefault(cashierId, new ArrayList<>());
        CartItemResponse item = cashierCart.stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            cashierCart.remove(item);
        } else {
            Inventory inv = inventoryRepository.findByProduct_Id(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (inv.getAvailableQuantity().compareTo(request.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient Stock! Only " + inv.getAvailableQuantity() + " left.");
            }
            updateItemCalculations(item, request.getQuantity(), inv.getProduct());
        }
        return generateCartSummary(cashierCart);
    }

    @Override
    public CartSummaryResponse removeItem(String cashierId, Long productId) {
        List<CartItemResponse> cashierCart = cartStore.getOrDefault(cashierId, new ArrayList<>());
        cashierCart.removeIf(item -> item.getProductId().equals(productId));
        return generateCartSummary(cashierCart);
    }

    @Override
    public CartSummaryResponse viewCart(String cashierId) {
        return generateCartSummary(cartStore.getOrDefault(cashierId, new ArrayList<>()));
    }

    @Override
    public void clearCart(String cashierId) {
        cartStore.remove(cashierId);
    }

    private void updateItemCalculations(CartItemResponse item, BigDecimal qty, Product product) {
        item.setQuantity(qty);
        item.setUnitPrice(product.getSellingPrice());

        BigDecimal subtotal = qty.multiply(product.getSellingPrice());
        item.setSubtotal(subtotal);

        // Safe conversion from Double to BigDecimal
        double gstValue = product.getGstPercentage() != null ? product.getGstPercentage() : 0.0;
        BigDecimal gstPercent = BigDecimal.valueOf(gstValue);
        item.setGstPercentage(gstPercent);

        BigDecimal gstAmount = subtotal.multiply(gstPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        item.setGstAmount(gstAmount);

        item.setDiscount(BigDecimal.ZERO);
        item.setFinalTotal(subtotal.add(gstAmount).subtract(item.getDiscount()));
    }

    private CartSummaryResponse generateCartSummary(List<CartItemResponse> cartItems) {
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (CartItemResponse item : cartItems) {
            totalQty = totalQty.add(item.getQuantity());
            subtotal = subtotal.add(item.getSubtotal());
            gstTotal = gstTotal.add(item.getGstAmount());
            grandTotal = grandTotal.add(item.getFinalTotal());
        }

        return CartSummaryResponse.builder()
                .items(cartItems)
                .totalItems(cartItems.size())
                .totalQuantity(totalQty)
                .subtotal(subtotal)
                .gstTotal(gstTotal)
                .discountTotal(BigDecimal.ZERO)
                .grandTotal(grandTotal)
                .build();
    }
}