package com.example.billing_backend.controller;

import com.example.billing_backend.dto.InventoryResponse;
import com.example.billing_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 🔥 FIX: Broadened Authority Checks
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'CASHIER', 'ROLE_CASHIER')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'CASHIER', 'ROLE_CASHIER')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PostMapping("/add/{productId}")
    public ResponseEntity<String> addStock(@PathVariable Long productId, @RequestParam BigDecimal quantity) {
        inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok("Stock added successfully!");
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PostMapping("/reduce/{productId}")
    public ResponseEntity<String> reduceStock(@PathVariable Long productId, @RequestParam BigDecimal quantity) {
        inventoryService.reduceStock(productId, quantity);
        return ResponseEntity.ok("Stock reduced successfully!");
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/reorder-level/{productId}")
    public ResponseEntity<String> updateReorderLevel(@PathVariable Long productId, @RequestParam BigDecimal newLevel) {
        inventoryService.updateReorderLevel(productId, newLevel);
        return ResponseEntity.ok("Reorder level updated successfully!");
    }
}