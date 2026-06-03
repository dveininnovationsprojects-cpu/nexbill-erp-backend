package com.example.billing_backend.service;

import com.example.billing_backend.dto.PaymentStatsResponse;
import com.example.billing_backend.model.Order;
import com.example.billing_backend.model.PaymentMode;
import com.example.billing_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    @Override
    public List<PaymentStatsResponse> getPaymentStatistics(LocalDate startDate, LocalDate endDate) {
        // Default to today if dates are not provided
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        // Convert LocalDate to LocalDateTime (Start of day to End of day)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return orderRepository.getPaymentAggregations(start, end);
    }

    @Override
    public List<Order> getTransactionsByMode(PaymentMode mode, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return orderRepository.findByPaymentModeAndCreatedAtBetweenOrderByCreatedAtDesc(mode, start, end);
    }
}