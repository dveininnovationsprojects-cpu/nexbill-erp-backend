package com.example.billing_backend.controller;

import com.example.billing_backend.dto.SupplierRequestDto;
import com.example.billing_backend.dto.SupplierResponseDto;
import com.example.billing_backend.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponseDto> addSupplier(@RequestBody SupplierRequestDto request) {
        return ResponseEntity.ok(supplierService.addSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponseDto> updateSupplier(@PathVariable Integer id, @RequestBody SupplierRequestDto request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleSupplierStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.toggleSupplierStatus(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<SupplierResponseDto> getSupplierById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<SupplierResponseDto>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }
}