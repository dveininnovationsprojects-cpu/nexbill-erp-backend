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

    @Column(nullable = false)
    @Builder.Default // 🔥 Lombok Builder use pannumbodhu default value apply aaga
    private String status = "COMPLETED"; // Status can be 'COMPLETED' or 'CANCELLED'

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

    // 🔥 FIX: Builder use pannumbodhu item list null aagama irukka Default empty array!
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    // 🔥 FIX: Once generate aana bill date maara koodadhu (updatable = false)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}