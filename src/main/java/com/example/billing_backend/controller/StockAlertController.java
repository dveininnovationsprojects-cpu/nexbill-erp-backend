package com.example.billing_backend.controller;

import com.example.billing_backend.dto.StockAlertResponse;
import com.example.billing_backend.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    // 🔥 Requires Token & Role matching
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/low-stock")
    public ResponseEntity<Page<StockAlertResponse>> getLowStock(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(stockAlertService.getLowStockAlerts(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/out-of-stock")
    public ResponseEntity<Page<StockAlertResponse>> getOutOfStock(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(stockAlertService.getOutOfStockAlerts(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/critical")
    public ResponseEntity<Page<StockAlertResponse>> getCriticalStock(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(stockAlertService.getCriticalStockAlerts(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/count")
    public ResponseEntity<Long> getLowStockCount() {
        return ResponseEntity.ok(stockAlertService.getLowStockCount());
    }
}