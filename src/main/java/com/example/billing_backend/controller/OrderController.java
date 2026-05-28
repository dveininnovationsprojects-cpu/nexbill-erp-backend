package com.example.billing_backend.controller;

import com.example.billing_backend.dto.OrderRequestDto;
import com.example.billing_backend.model.Order;
import com.example.billing_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<Order> processCheckout(
            @RequestBody OrderRequestDto request,
            Principal principal) {

        // Principal extracts the currently logged-in Cashier's email
        Order savedOrder = orderService.createOrder(request, principal.getName());
        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }
}