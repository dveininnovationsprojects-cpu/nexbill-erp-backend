package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;      // Selling Price
    private BigDecimal subtotal;       // quantity * unitPrice
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;      // (subtotal * gstPercentage) / 100
    private BigDecimal discount;       // Any specific discount
    private BigDecimal finalTotal;     // subtotal + gstAmount - discount
}