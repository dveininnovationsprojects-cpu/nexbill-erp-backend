package com.example.billing_backend.repository;

import com.example.billing_backend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // =======================================================
    // 🔥 ENTERPRISE PERFORMANCE QUERIES (Tax Module - OOM Preventer)
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

    // =======================================================
    // 🔥 FRONTEND FIXES (Cashier Access)
    // =======================================================

    // Issue 5 Fix: History Access (Order by latest first)
    List<Invoice> findAllByOrderByCreatedAtDesc();

    List<Invoice> findByCashierIdOrderByCreatedAtDesc(String cashierId);
}