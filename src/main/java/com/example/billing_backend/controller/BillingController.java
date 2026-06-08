
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
        return ResponseEntity.ok(billingService.checkout(principal.getName(), request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PostMapping("/create")
    public ResponseEntity<BillResponse> createDirectInvoice(Principal principal, @RequestBody BillRequest request) {
        return ResponseEntity.ok(billingService.createDirectInvoice(principal.getName(), request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Invoice> getBill(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getBillByInvoiceNumber(invoiceNumber));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/history")
    public ResponseEntity<List<Invoice>> getAllBills(Principal principal) {
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        return ResponseEntity.ok(billingService.getAllBillsForUser(principal.getName(), role));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/cancel/{invoiceNumber}")
    public ResponseEntity<String> cancelInvoice(@PathVariable String invoiceNumber, Principal principal) {
        return ResponseEntity.ok(billingService.cancelInvoice(invoiceNumber, principal.getName()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PutMapping("/pay/{invoiceNumber}")
    public ResponseEntity<String> markAsPaid(@PathVariable String invoiceNumber, Principal principal) {
        return ResponseEntity.ok(billingService.markAsPaid(invoiceNumber, principal.getName()));
    }
}