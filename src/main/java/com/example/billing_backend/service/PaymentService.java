package com.example.billing_backend.service;

import com.example.billing_backend.dto.PaymentStatsResponse;
import com.example.billing_backend.model.Order;
import com.example.billing_backend.model.PaymentMode;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    List<PaymentStatsResponse> getPaymentStatistics(LocalDate startDate, LocalDate endDate);
    List<Order> getTransactionsByMode(PaymentMode mode, LocalDate startDate, LocalDate endDate);
}