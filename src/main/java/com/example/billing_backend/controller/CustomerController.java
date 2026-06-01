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
@CrossOrigin(origins = "*") // Frontend dynamic dashboard connection loop visibility rules check mapping
public class CustomerController {

    private final CustomerService customerService;

    // 1. ADD CUSTOMER (Both can register new retail profiles)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> addCustomer(@RequestBody CustomerRequestDto request) {
        return ResponseEntity.ok(customerService.addCustomer(request));
    }

    // 2. SEARCH BY MOBILE NUMBER (Both can verify profiles on live counter)
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> searchCustomer(@RequestParam String mobile) {
        return ResponseEntity.ok(customerService.getCustomerByMobile(mobile));
    }

    // 3. GET ALL CUSTOMERS (🔥 OPEN TO CASHIER ALSO: Cashier can now browse complete ledger logs)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // 4. UPDATE CUSTOMER METADATA (🔥 OPEN TO CASHIER ALSO: Cashier can edit typos/wrong names immediately)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> updateCustomer(@PathVariable Integer id, @RequestBody CustomerRequestDto request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // 5. UPDATE LEDGER DUE BALANCE (Both can increment bills or clear pending dues)
    @PutMapping("/{id}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> updateLedger(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "0") Double bill,
            @RequestParam(required = false, defaultValue = "0") Double paid) {
        return ResponseEntity.ok(customerService.updateCustomerLedger(id, bill, paid));
    }

    // 6. UPDATE STATUS ACTIVE/BLACKLISTED (🔥 OPEN TO CASHIER ALSO: Cashier can instantly freeze high debt breaches)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<CustomerResponseDto> changeStatus(
            @PathVariable Integer id,
            @RequestParam com.example.billing_backend.model.CustomerStatus status) {
        return ResponseEntity.ok(customerService.changeCustomerStatus(id, status));
    }

    // 7. DELETE CUSTOMER PROFILE ENTRY (🚨 EXPLICIT NOTE: Kept strictly for ADMIN only!)
    // Business rule standard constraints-padi audit row trails complete track loss aaga koodathu,
    // so raw profile table entry deletion access validation standard layout structure logic-padi Admin kitterye irukatum machan.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    // Local exception handler mapping engine block
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> handleExceptions(IllegalArgumentException e) {
        java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}