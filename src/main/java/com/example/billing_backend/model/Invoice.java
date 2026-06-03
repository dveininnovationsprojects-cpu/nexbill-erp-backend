package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private String cashierId;

    // ==========================================
    // 🔥 CUSTOMER & STATUS DETAILS
    // ==========================================
    private String customerName;
    private String customerPhone;

    // 🔥 PUDHUSA ADD PANNA VENDIYA FIELD FOR FRONTEND (Issue 1)
    private String customerEmail;

    @Column(nullable = false)
    @Builder.Default
    private String status = "COMPLETED";

    // ==========================================
    // 🔥 BILLING MATH DETAILS
    // ==========================================
    private int totalItems;
    private BigDecimal totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal gstTotal;
    private BigDecimal discountTotal;

    @Column(nullable = false)
    private BigDecimal grandTotal;

    @Column(nullable = false)
    private String paymentMethod;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}