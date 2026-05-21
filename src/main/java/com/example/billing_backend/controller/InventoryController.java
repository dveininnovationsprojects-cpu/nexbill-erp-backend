package com.example.billing_backend.controller;

import com.example.billing_backend.dto.AddStockRequest;
import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    // ADMIN: New stock add panna (1.5, 2.5 etc. supported)
    // EXACT MATCH: hasAuthority('ROLE_ADMIN') checks exactly what User.java generates!
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add-stock")
    public ResponseEntity<String> addStock(@RequestBody AddStockRequest request) {
        inventoryService.addStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("Stock added successfully!");
    }

    // ADMIN & CASHIER: Specific product inventory paarka
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    // ADMIN: Low stock check panna
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockProducts(@RequestParam Double threshold) {
        return ResponseEntity.ok(inventoryService.getLowStockProducts(threshold));
    }

    // ADMIN: Reorder level update panna
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/reorder-level/{productId}")
    public ResponseEntity<String> updateReorderLevel(@PathVariable Long productId, @RequestParam Double newLevel) {
        inventoryService.updateReorderLevel(productId, newLevel);
        return ResponseEntity.ok("Reorder level updated successfully!");
    }
}