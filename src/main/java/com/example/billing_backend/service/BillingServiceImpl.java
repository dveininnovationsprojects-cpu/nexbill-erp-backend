package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.dto.CartItemResponse;
import com.example.billing_backend.dto.CartSummaryResponse;
import com.example.billing_backend.model.*;
import com.example.billing_backend.repository.CustomerRepository;
import com.example.billing_backend.repository.InventoryRepository;
import com.example.billing_backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final CartService cartService;
    private final InventoryRepository inventoryRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public BillResponse checkout(String cashierId, BillRequest request) {

        CartSummaryResponse cartSummary = cartService.viewCart(cashierId);
        if (cartSummary.getItems() == null || cartSummary.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty! Cannot generate bill.");
        }

        // Resolve customer name
        Customer customer = null;
        String customerName = "Walk-in Customer";
        String customerPhone = "";
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        }
        if (customer != null) {
            customerName = customer.getName();
            customerPhone = customer.getMobile() != null ? customer.getMobile() : "";
        } else if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            customerName = request.getCustomerName();
            customerPhone = request.getCustomerPhone() != null ? request.getCustomerPhone() : "";
        }

        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Apply discount
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalGrandTotal = cartSummary.getGrandTotal().subtract(discountAmount).max(BigDecimal.ZERO);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .totalItems(cartSummary.getTotalItems())
                .totalQuantity(cartSummary.getTotalQuantity())
                .subtotal(cartSummary.getSubtotal())
                .gstTotal(cartSummary.getGstTotal())
                .discountTotal(discountAmount)
                .grandTotal(finalGrandTotal)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
                .items(new ArrayList<>())
                .build();

        invoice = invoiceRepository.save(invoice);

        for (CartItemResponse cartItem : cartSummary.getItems()) {
            Inventory inv = inventoryRepository.findByProduct_IdForUpdate(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductName()));

            if (inv.getAvailableQuantity().compareTo(cartItem.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient stock during checkout for product: " + cartItem.getProductName());
            }

            inv.setAvailableQuantity(inv.getAvailableQuantity().subtract(cartItem.getQuantity()));
            boolean isLow = inv.getAvailableQuantity().compareTo(inv.getReorderLevel()) <= 0;
            inv.setLowStockAlert(isLow);
            inventoryRepository.save(inv);

            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .sku(cartItem.getSku())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subtotal(cartItem.getSubtotal())
                    .gstPercentage(cartItem.getGstPercentage())
                    .gstAmount(cartItem.getGstAmount())
                    .discount(cartItem.getDiscount())
                    .finalTotal(cartItem.getFinalTotal())
                    .build();

            invoice.getItems().add(invoiceItem);
        }

        invoiceRepository.save(invoice);
        cartService.clearCart(cashierId);

        // Update customer totalSpentAmount and tier
        if (customer != null) {
            double spent = (customer.getTotalSpentAmount() != null ? customer.getTotalSpentAmount() : 0.0)
                    + finalGrandTotal.doubleValue();
            customer.setTotalSpentAmount(spent);
            customer.setTier(spent >= 50000 ? CustomerTier.CORPORATE : spent >= 10000 ? CustomerTier.VIP : CustomerTier.REGULAR);
            customer.setLastCreditDateTime(LocalDateTime.now());
            customerRepository.save(customer);
        }

        return BillResponse.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .grandTotal(invoice.getGrandTotal())
                .paymentMethod(invoice.getPaymentMethod())
                .message("Bill generated successfully!")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // =========================================================
    // 🔥 ADDITIONS FOR BILLING HISTORY & REPRINT
    // =========================================================

    @Override
    public Invoice getBillByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));
    }

    @Override
    public List<Invoice> getAllBills() {
        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoice> getInvoicesByCashier(String cashierId) {
        return invoiceRepository.findByCashierIdOrderByCreatedAtDesc(cashierId);
    }

    // =========================================================
    // 🔥 NEW ADDITION: CANCEL INVOICE & RESTOCK LOGIC
    // =========================================================

    @Override
    @Transactional
    public String cancelInvoice(String invoiceNumber, String cancelledBy) {

        // 1. Find the Invoice
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        // 2. Validation: Check if already cancelled
        if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new RuntimeException("This invoice is already cancelled!");
        }

        // 3. Restock the Inventory using Pessimistic Lock for safety
        for (InvoiceItem item : invoice.getItems()) {
            Inventory inv = inventoryRepository.findByProduct_IdForUpdate(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for restocking product: " + item.getProductName()));

            // Add the returned quantity back to available stock
            inv.setAvailableQuantity(inv.getAvailableQuantity().add(item.getQuantity()));

            // Re-evaluate low stock alert after restocking
            boolean isLow = inv.getAvailableQuantity().compareTo(inv.getReorderLevel()) <= 0;
            inv.setLowStockAlert(isLow);

            inventoryRepository.save(inv);
        }

        // 4. Update Invoice Status
        invoice.setStatus("CANCELLED");
        // Note: If you added a 'cancelledBy' field in Invoice.java later, you can set it here.

        invoiceRepository.save(invoice);

        return "Invoice " + invoiceNumber + " has been successfully CANCELLED and stock has been restored!";
    }
}