package com.example.billing_backend.service;

import com.example.billing_backend.dto.*;
import com.example.billing_backend.repository.OrderItemRepository;
import com.example.billing_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public DashboardSummaryDto getDashboardSummary(LocalDateTime startDate, LocalDateTime endDate) {

        // 1. Fetch Aggregated Totals (Handling Nulls dynamically)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(startDate, endDate);
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;

        BigDecimal totalTax = orderRepository.calculateTotalTax(startDate, endDate);
        totalTax = totalTax != null ? totalTax : BigDecimal.ZERO;

        BigDecimal totalDiscount = orderRepository.calculateTotalDiscount(startDate, endDate);
        totalDiscount = totalDiscount != null ? totalDiscount : BigDecimal.ZERO;

        Long totalInvoices = orderRepository.countTotalInvoices(startDate, endDate);

        // 2. Fetch Grouped Data
        List<PaymentModeBreakdownDto> paymentBreakdown = orderRepository.getPaymentModeBreakdown(startDate, endDate);
        List<CashierPerformanceDto> cashierPerformances = orderRepository.getCashierPerformance(startDate, endDate);

        // Fetch Top 5 Selling Products using Pagination trick
        List<TopSellingProductDto> topProducts = orderItemRepository.findTopSellingProducts(startDate, endDate, PageRequest.of(0, 5));

        // 3. Assemble and Return the Master DTO
        return DashboardSummaryDto.builder()
                .totalGrossRevenue(totalRevenue)
                .totalInvoicesGenerated(totalInvoices)
                .totalTaxCollected(totalTax)
                .totalDiscountsGiven(totalDiscount)
                .paymentBreakdown(paymentBreakdown)
                .topProducts(topProducts)
                .cashierPerformances(cashierPerformances)
                .build();
    }

    // ADVANCED: CSV GENERATOR LOGIC
    @Override
    public String generateSalesCsvReport(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardSummaryDto data = getDashboardSummary(startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("NEXBILL ERP - SALES REPORT\n");
        csv.append("From: ").append(startDate).append(" To: ").append(endDate).append("\n\n");

        csv.append("--- SUMMARY ---\n");
        csv.append("Total Revenue,Total Invoices,Total Tax,Total Discounts\n");
        csv.append(data.getTotalGrossRevenue()).append(",")
                .append(data.getTotalInvoicesGenerated()).append(",")
                .append(data.getTotalTaxCollected()).append(",")
                .append(data.getTotalDiscountsGiven()).append("\n\n");

        csv.append("--- PAYMENT MODE BREAKDOWN ---\n");
        csv.append("Payment Mode,Total Amount,Transaction Count\n");
        for (PaymentModeBreakdownDto p : data.getPaymentBreakdown()) {
            csv.append(p.getPaymentMode()).append(",").append(p.getTotalAmount()).append(",").append(p.getTransactionCount()).append("\n");
        }

        csv.append("\n--- TOP 5 PRODUCTS ---\n");
        csv.append("Product Name,Quantity Sold,Revenue Generated\n");
        for (TopSellingProductDto p : data.getTopProducts()) {
            csv.append(p.getProductName()).append(",").append(p.getTotalQuantitySold()).append(",").append(p.getTotalRevenueGenerated()).append("\n");
        }

        return csv.toString();
    }
}