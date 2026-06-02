package com.example.billing_backend.repository;

import com.example.billing_backend.dto.TopProductDto;
import com.example.billing_backend.dto.TopSellingProductDto;
import com.example.billing_backend.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Advanced: Group by Product and Sum Quantity & Revenue, sorted descending
    @Query("SELECT new com.example.billing_backend.dto.TopSellingProductDto(p.name, SUM(oi.quantity), SUM(oi.lineTotal)) " +
            "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED' " +
            "GROUP BY p.name ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingProductDto> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    // Net Profit = SUM( (SellingPrice - PurchasePrice) * Quantity )
    @Query("SELECT SUM((oi.unitPrice - p.purchasePrice) * oi.quantity) FROM OrderItem oi JOIN oi.product p JOIN oi.order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'COMPLETED'")
    BigDecimal calculateNetProfit(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Fast Moving Products (By Quantity)
    @Query("SELECT new com.example.billing_backend.dto.TopProductDto(p.name, SUM(oi.quantity)) FROM OrderItem oi JOIN oi.product p JOIN oi.order o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY SUM(oi.quantity) DESC")
    List<TopProductDto> findFastMovingProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // High Revenue Yielding Products
    @Query("SELECT new com.example.billing_backend.dto.TopProductDto(p.name, SUM(oi.lineTotal)) FROM OrderItem oi JOIN oi.product p JOIN oi.order o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY SUM(oi.lineTotal) DESC")
    List<TopProductDto> findHighRevenueProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Dead Stock: Products NOT present in OrderItem in the last 90 days
    @Query("SELECT COUNT(p) FROM Product p WHERE p.id NOT IN (SELECT distinct oi.product.id FROM OrderItem oi JOIN oi.order o WHERE o.createdAt >= :ninetyDaysAgo)")
    Long countDeadStock(@Param("ninetyDaysAgo") LocalDateTime ninetyDaysAgo);
}