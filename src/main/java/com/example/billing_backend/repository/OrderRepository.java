package com.example.billing_backend.repository;

import com.example.billing_backend.dto.CashierPerformanceDto;
import com.example.billing_backend.dto.PaymentModeBreakdownDto;
import com.example.billing_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Total Revenue in Date Range
    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 2. Total Invoices in Date Range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    Long countTotalInvoices(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 3. Tax and Discount Aggregations
    @Query("SELECT SUM(o.taxAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    BigDecimal calculateTotalTax(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.discountAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    BigDecimal calculateTotalDiscount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 4. Payment Mode Grouping (Advanced)
    @Query("SELECT new com.example.billing_backend.dto.PaymentModeBreakdownDto(o.paymentMode, SUM(o.grandTotal), COUNT(o)) " +
            "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED' " +
            "GROUP BY o.paymentMode")
    List<PaymentModeBreakdownDto> getPaymentModeBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 5. Cashier Metrics Evaluation
    @Query("SELECT new com.example.billing_backend.dto.CashierPerformanceDto(u.name, u.email, COUNT(o), SUM(o.grandTotal)) " +
            "FROM Order o JOIN o.cashier u WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED' " +
            "GROUP BY u.name, u.email ORDER BY SUM(o.grandTotal) DESC")
    List<CashierPerformanceDto> getCashierPerformance(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.example.billing_backend.dto.PaymentStatsResponse(o.paymentMode, SUM(o.grandTotal), COUNT(o)) " +
            "FROM Order o " +
            "WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY o.paymentMode")
    java.util.List<com.example.billing_backend.dto.PaymentStatsResponse> getPaymentAggregations(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate
    );

    java.util.List<com.example.billing_backend.model.Order> findByPaymentModeAndCreatedAtBetweenOrderByCreatedAtDesc(
            com.example.billing_backend.model.PaymentMode paymentMode,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate
    );
}