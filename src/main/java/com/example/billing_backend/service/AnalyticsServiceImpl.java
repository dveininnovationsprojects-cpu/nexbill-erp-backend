package com.example.billing_backend.service;

import com.example.billing_backend.dto.*;
import com.example.billing_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;

    public AdvancedDashboardDto getMegaDashboardData(LocalDateTime startDate, LocalDateTime endDate) {

        // 1. FINANCIAL KPIS
        BigDecimal grossRevenue = orderRepository.calculateTotalRevenue(startDate, endDate);
        grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;

        BigDecimal netProfit = orderItemRepository.calculateNetProfit(startDate, endDate);
        netProfit = netProfit != null ? netProfit : BigDecimal.ZERO;

        BigDecimal totalDiscounts = orderRepository.calculateTotalDiscount(startDate, endDate);
        totalDiscounts = totalDiscounts != null ? totalDiscounts : BigDecimal.ZERO;

        Long invoiceCount = orderRepository.countTotalInvoices(startDate, endDate);
        BigDecimal aov = invoiceCount > 0 ? grossRevenue.divide(BigDecimal.valueOf(invoiceCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 2. INVENTORY METRICS
        Long oosCount = inventoryRepository.countOutOfStockItems();
        Long deadStockCount = orderItemRepository.countDeadStock(LocalDateTime.now().minusDays(90));
        var categoryValuations = inventoryRepository.getCategoryValuations();

        // 3. CASHIER AUDITS
        var topCashiers = orderRepository.getCashierPerformance(startDate, endDate);

        // 4. CUSTOMER INSIGHTS
        var topSpenders = customerRepository.findTopSpenders(PageRequest.of(0, 5));
        Long newCustomers = customerRepository.countNewAcquisitions(startDate, endDate);

        // 5. PRODUCT ANALYTICS
        var fastMoving = orderItemRepository.findFastMovingProducts(startDate, endDate, PageRequest.of(0, 5));
        var highYield = orderItemRepository.findHighRevenueProducts(startDate, endDate, PageRequest.of(0, 5));

        // ASSEMBLE MASTER DTO
        return AdvancedDashboardDto.builder()
                .grossRevenue(grossRevenue)
                .netProfitMargin(netProfit)
                .averageTicketSize(aov)
                .totalDiscountsGiven(totalDiscounts)
                .outOfStockCount(oosCount)
                .categoryValuations(categoryValuations)
                .deadStockCount(deadStockCount)
                .topCashiers(topCashiers)
                .topSpenders(topSpenders)
                .newCustomersAcquired(newCustomers)
                .fastMovingProducts(fastMoving)
                .highRevenueProducts(highYield)
                .build();
    }
}