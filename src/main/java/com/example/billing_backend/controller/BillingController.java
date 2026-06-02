package com.example.billing_backend.controller;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PostMapping("/checkout")
    public ResponseEntity<BillResponse> checkout(Principal principal, @RequestBody BillRequest request) {
        String cashierId = principal.getName();
        return ResponseEntity.ok(billingService.checkout(cashierId, request));
    }

    // 🔥 Get Specific Bill
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Invoice> getBill(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getBillByInvoiceNumber(invoiceNumber));
    }

    // =========================================================
    // 🔥 FRONTEND ISSUE 5 FIX: Both Admin & Cashier can access their respective histories
    // =========================================================
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')") // Changed from just ROLE_ADMIN
    @GetMapping("/history")
    public ResponseEntity<List<Invoice>> getAllBills(Principal principal) {
        // Fetch user authorities from SecurityContext to pass the role
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();

        return ResponseEntity.ok(billingService.getAllBillsForUser(principal.getName(), role));
    }

    // =========================================================
    // 🔥 CANCEL / SOFT DELETE INVOICE & RESTOCK (Admin Only)
    // =========================================================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/cancel/{invoiceNumber}")
    public ResponseEntity<String> cancelInvoice(
            @PathVariable String invoiceNumber,
            Principal principal) {

        String responseMessage = billingService.cancelInvoice(invoiceNumber, principal.getName());
        return ResponseEntity.ok(responseMessage);
    }
}