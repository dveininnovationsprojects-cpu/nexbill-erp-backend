package com.example.billing_backend.controller;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 🔥 PUDHUSA ADD PANNADHU: Get Specific Bill
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Invoice> getBill(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getBillByInvoiceNumber(invoiceNumber));
    }

    // 🔥 PUDHUSA ADD PANNADHU: Get All Bills (Admin Only access is better here)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("/history")
    public ResponseEntity<List<Invoice>> getAllBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }
}