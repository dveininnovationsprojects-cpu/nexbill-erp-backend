package com.example.billing_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartSummaryResponse {
    private List<CartItemResponse> items;
    private int totalItems;            // Count of different products
    private BigDecimal totalQuantity;  // Sum of all quantities
    private BigDecimal subtotal;       // Sum of all subtotals
    private BigDecimal gstTotal;       // Sum of all GST amounts
    private BigDecimal discountTotal;  // Sum of all discounts
    private BigDecimal grandTotal;     // Final payable amount
}