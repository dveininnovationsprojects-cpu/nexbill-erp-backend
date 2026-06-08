package com.example.billing_backend.repository;

import com.example.billing_backend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Cashier own invoices
    List<Invoice> findByCashierIdOrderByCreatedAtDesc(String cashierId);

    // Cashier invoices by date range
    @Query("SELECT i FROM Invoice i WHERE i.cashierId = :cashierId AND i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    List<Invoice> findByCashierIdAndDateRange(@Param("cashierId") String cashierId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // =======================================================
    // 🔥 ENTERPRISE PERFORMANCE QUERIES (OOM Preventer)
    // =======================================================

    @Query("SELECT COUNT(i) FROM Invoice i")
    long countTotalInvoices();

    @Query("SELECT COALESCE(SUM(i.subtotal), 0) FROM Invoice i")
    BigDecimal sumTotalSubtotal();

    @Query("SELECT COALESCE(SUM(i.discountTotal), 0) FROM Invoice i")
    BigDecimal sumTotalDiscount();

    @Query("SELECT COALESCE(SUM(i.gstTotal), 0) FROM Invoice i")
    BigDecimal sumTotalGst();

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i")
    BigDecimal sumTotalRevenue();
}