package com.example.billing_backend.service;

import com.example.billing_backend.dto.OrderRequestDto;
import com.example.billing_backend.model.Order;

public interface OrderService {
    Order createOrder(OrderRequestDto request, String cashierEmail);
}