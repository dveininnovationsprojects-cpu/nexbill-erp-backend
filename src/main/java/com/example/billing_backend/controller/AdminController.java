package com.example.billing_backend.controller;

import com.example.billing_backend.dto.CashierApprovalRequest;
import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.model.UserStatus;
import com.example.billing_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/pending-cashiers")
    public ResponseEntity<List<User>> getPendingCashiers() {
        return ResponseEntity.ok(userRepository.findByRoleAndStatus(Role.CASHIER, UserStatus.PENDING));
    }

    @PostMapping("/approve-cashier/{email}")
    public ResponseEntity<String> approveCashier(
            @PathVariable String email,
            @RequestBody CashierApprovalRequest request) {

        User cashier = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cashier.setPhone(request.getPhone());
        cashier.setBranch(request.getBranch());
        cashier.setCounterNumber(request.getCounterNumber());
        cashier.setShiftTiming(request.getShiftTiming());
        cashier.setBasicSalary(request.getBasicSalary());
        cashier.setStatus(UserStatus.ACTIVE);

        userRepository.save(cashier);

        return ResponseEntity.ok("Cashier approved and details updated successfully");
    }
    @GetMapping("/active-cashiers")
    public ResponseEntity<List<User>> getActiveCashiers() {
        return ResponseEntity.ok(userRepository.findByRoleAndStatus(Role.CASHIER, UserStatus.ACTIVE));
    }
    @GetMapping("/all-cashiers")
    public ResponseEntity<List<User>> getAllCashiers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.CASHIER));
    }
    // 🔥 AUTOMATION ENGINE: Dynamic Staff Status Control Toggle (ACTIVE / INACTIVE / SUSPENDED)
    @PutMapping("/cashier/{id}/toggle-status")
    public ResponseEntity<String> toggleCashierStatus(
            @PathVariable Integer id,
            @RequestParam UserStatus status) {

        User cashier = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cashier profile entry not found!"));

        // Force check: Admin role-ah admin-ey moola-ma maatha koodathu
        if (cashier.getRole() == Role.ADMIN) {
            throw new RuntimeException("Security Breach: Admin status cannot be altered via this endpoint!");
        }

        cashier.setStatus(status);
        userRepository.save(cashier);

        return ResponseEntity.ok("Cashier status updated successfully to: " + status);
    }
}