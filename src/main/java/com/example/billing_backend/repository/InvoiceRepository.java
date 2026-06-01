package com.example.billing_backend.repository;

import com.example.billing_backend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // 🔥 PUDHUSA ADD PANNADHU
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}