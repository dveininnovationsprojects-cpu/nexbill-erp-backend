package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillItemRequest;
import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.dto.CartItemResponse;
import com.example.billing_backend.dto.CartSummaryResponse;
import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.model.InvoiceItem;
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

    // =========================================================
    // 🔥 CASHIER CHECKOUT (Cart-based)
    // =========================================================
    @Override
    @Transactional
    public BillResponse checkout(String cashierId, BillRequest request) {

        CartSummaryResponse cartSummary = cartService.viewCart(cashierId);
        if (cartSummary.getItems() == null || cartSummary.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty! Cannot generate bill.");
        }

        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal calculatedGrandTotal = cartSummary.getSubtotal()
                .add(cartSummary.getGstTotal())
                .subtract(cartSummary.getDiscountTotal());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .totalItems(cartSummary.getTotalItems())
                .totalQuantity(cartSummary.getTotalQuantity())
                .subtotal(cartSummary.getSubtotal())
                .gstTotal(cartSummary.getGstTotal())
                .discountTotal(cartSummary.getDiscountTotal())
                .grandTotal(calculatedGrandTotal)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
                .items(new ArrayList<>())
                .status("COMPLETED")
                .build();

        String cName = request.getCustomerName();
        invoice.setCustomerName((cName != null && !cName.trim().isEmpty()) ? cName : "Walk-in Customer");
        invoice.setCustomerPhone(request.getCustomerPhone());
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
            invoice.setCustomerEmail(request.getCustomerEmail());
        }

        invoice = invoiceRepository.save(invoice);

        for (CartItemResponse cartItem : cartSummary.getItems()) {
            Inventory inv = inventoryRepository.findByProduct_IdForUpdate(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductName()));

            if (inv.getAvailableQuantity().compareTo(cartItem.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient stock for: " + cartItem.getProductName());
            }

            inv.setAvailableQuantity(inv.getAvailableQuantity().subtract(cartItem.getQuantity()));
            inv.setLowStockAlert(inv.getAvailableQuantity().compareTo(inv.getReorderLevel()) <= 0);
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

        return BillResponse.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .customerName(invoice.getCustomerName())
                .grandTotal(invoice.getGrandTotal())
                .paymentMethod(invoice.getPaymentMethod())
                .message("Bill generated successfully!")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // =========================================================
    // 🔥 ADMIN DIRECT INVOICE CREATION (No cart needed)
    // =========================================================
    @Override
    @Transactional
    public BillResponse createDirectInvoice(String cashierId, BillRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("No items provided!");
        }

        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (BillItemRequest item : request.getItems()) {
            BigDecimal lineAmt = BigDecimal.valueOf(item.getRate())
                    .multiply(BigDecimal.valueOf(item.getQty()));
            BigDecimal lineGst = lineAmt
                    .multiply(BigDecimal.valueOf(item.getGst()))
                    .divide(BigDecimal.valueOf(100));
            subtotal = subtotal.add(lineAmt);
            gstTotal = gstTotal.add(lineGst);
        }

        BigDecimal discountAmt = BigDecimal.ZERO;
        if (request.getDiscount() != null && request.getDiscount() > 0) {
            discountAmt = subtotal.add(gstTotal)
                    .multiply(BigDecimal.valueOf(Math.min(request.getDiscount(), 100)))
                    .divide(BigDecimal.valueOf(100));
        }

        BigDecimal grandTotal = subtotal.add(gstTotal).subtract(discountAmt);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .totalItems(request.getItems().size())
                .totalQuantity(BigDecimal.valueOf(
                        request.getItems().stream().mapToInt(BillItemRequest::getQty).sum()))
                .subtotal(subtotal)
                .gstTotal(gstTotal)
                .discountTotal(discountAmt)
                .grandTotal(grandTotal)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH")
                .customerName(request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()
                        ? request.getCustomerName() : "Walk-in Customer")
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .items(new ArrayList<>())
                .build();

        invoice = invoiceRepository.save(invoice);

        for (BillItemRequest item : request.getItems()) {
            BigDecimal lineAmt = BigDecimal.valueOf(item.getRate())
                    .multiply(BigDecimal.valueOf(item.getQty()));
            BigDecimal lineGst = lineAmt
                    .multiply(BigDecimal.valueOf(item.getGst()))
                    .divide(BigDecimal.valueOf(100));

            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .productId(item.getProductId())
                    .productName(item.getName())
                    .quantity(BigDecimal.valueOf(item.getQty()))
                    .unitPrice(BigDecimal.valueOf(item.getRate()))
                    .subtotal(lineAmt)
                    .gstPercentage(BigDecimal.valueOf(item.getGst()))
                    .gstAmount(lineGst)
                    .discount(BigDecimal.ZERO)
                    .finalTotal(lineAmt.add(lineGst))
                    .build();

            invoice.getItems().add(invoiceItem);
        }

        invoiceRepository.save(invoice);

        return BillResponse.builder()
                .invoiceNumber(invoiceNumber)
                .cashierId(cashierId)
                .customerName(invoice.getCustomerName())
                .grandTotal(invoice.getGrandTotal())
                .paymentMethod(invoice.getPaymentMethod())
                .status(invoice.getStatus())
                .message("Invoice created successfully!")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // =========================================================
    // 🔥 GET BILL BY INVOICE NUMBER
    // =========================================================
    @Override
    public Invoice getBillByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));
    }

    // =========================================================
    // 🔥 GET ALL BILLS FOR USER
    // =========================================================
    @Override
    public List<Invoice> getAllBillsForUser(String userEmail, String role) {
        if (role.contains("ROLE_ADMIN") || role.contains("ADMIN")) {
            return invoiceRepository.findAllByOrderByCreatedAtDesc();
        }
        return invoiceRepository.findByCashierIdOrderByCreatedAtDesc(userEmail);
    }

    // =========================================================
    // 🔥 CANCEL INVOICE & RESTOCK
    // =========================================================
    @Override
    @Transactional
    public String cancelInvoice(String invoiceNumber, String cancelledBy) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new RuntimeException("This invoice is already cancelled!");
        }

        for (InvoiceItem item : invoice.getItems()) {
            if (item.getProductId() != null) {
                Inventory inv = inventoryRepository.findByProduct_IdForUpdate(item.getProductId())
                        .orElse(null);
                if (inv != null) {
                    inv.setAvailableQuantity(inv.getAvailableQuantity().add(item.getQuantity()));
                    inv.setLowStockAlert(inv.getAvailableQuantity().compareTo(inv.getReorderLevel()) <= 0);
                    inventoryRepository.save(inv);
                }
            }
        }

        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);

        return "Invoice " + invoiceNumber + " cancelled and stock restored!";
    }

    // =========================================================
    // 🔥 MARK INVOICE AS PAID
    // =========================================================
    @Override
    @Transactional
    public String markAsPaid(String invoiceNumber, String updatedBy) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new RuntimeException("Cannot mark a cancelled invoice as paid.");
        }

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new RuntimeException("Invoice is already paid.");
        }

        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        return "Invoice " + invoiceNumber + " marked as paid!";
    }
}