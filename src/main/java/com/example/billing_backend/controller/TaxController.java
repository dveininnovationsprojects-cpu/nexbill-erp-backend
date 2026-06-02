package com.example.billing_backend.controller;

import com.example.billing_backend.dto.GstReportResponse;
import com.example.billing_backend.dto.TaxCalculationRequest;
import com.example.billing_backend.dto.TaxSummaryResponse;
import com.example.billing_backend.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tax")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TaxController {

    private final TaxCalculationService taxCalculationService;


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @PostMapping("/calculate")
    public ResponseEntity<TaxSummaryResponse> calculateTax(@RequestBody TaxCalculationRequest request) {
        return ResponseEntity.ok(taxCalculationService.calculateTax(request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/summary/{invoiceNumber}")
    public ResponseEntity<TaxSummaryResponse> getTaxSummary(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(taxCalculationService.getTaxSummaryForBill(invoiceNumber));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("/report")
    public ResponseEntity<GstReportResponse> getGstReport() {
        return ResponseEntity.ok(taxCalculationService.getGstReport());
    }
}