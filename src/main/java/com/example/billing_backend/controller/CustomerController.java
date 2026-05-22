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
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> handleExceptions(IllegalArgumentException e) {
        java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponseDto> changeStatus(
            @PathVariable Integer id,
            @RequestParam com.example.billing_backend.model.CustomerStatus status) {
        return ResponseEntity.ok(customerService.changeCustomerStatus(id, status));
    }
}