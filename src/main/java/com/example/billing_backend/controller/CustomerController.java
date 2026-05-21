package com.example.billing_backend.controller;

import com.example.billing_backend.dto.CustomerRequestDto;
import com.example.billing_backend.dto.CustomerResponseDto;
import com.example.billing_backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> addCustomer(@RequestBody CustomerRequestDto request) {
        return ResponseEntity.ok(customerService.addCustomer(request));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> searchCustomer(@RequestParam String mobile) {
        return ResponseEntity.ok(customerService.getCustomerByMobile(mobile));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponseDto> updateCustomer(@PathVariable Integer id, @RequestBody CustomerRequestDto request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @PutMapping("/{id}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> updateLedger(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "0") Double bill,
            @RequestParam(required = false, defaultValue = "0") Double paid) {
        return ResponseEntity.ok(customerService.updateCustomerLedger(id, bill, paid));
    }
}