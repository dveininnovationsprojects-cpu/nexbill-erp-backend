package com.example.billing_backend.controller;

import com.example.billing_backend.dto.PaymentStatsResponse;
import com.example.billing_backend.model.Order;
import com.example.billing_backend.model.PaymentMode;
import com.example.billing_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // 1. GET AGGREGATE DASHBOARD STATS
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<PaymentStatsResponse>> getPaymentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(paymentService.getPaymentStatistics(startDate, endDate));
    }

    // 2. GET DETAILED TRANSACTIONS LIST FOR SPECIFIC MODE
    @GetMapping("/transactions/{mode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<Order>> getTransactionsByMode(
            @PathVariable PaymentMode mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(paymentService.getTransactionsByMode(mode, startDate, endDate));
    }
}