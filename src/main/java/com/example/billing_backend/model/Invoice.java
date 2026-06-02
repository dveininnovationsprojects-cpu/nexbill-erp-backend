package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    // 🔥 PUDHUSA ADD PANNA FIELDS (For PDF & Audit)
    // ==========================================
    private String customerName;

    private String customerPhone;

    @Column(nullable = false)
    @Builder.Default // 🔥 Lombok Builder use pannumbodhu default value apply aaga idhu romba mukkiyam!
    private String status = "COMPLETED"; // Status can be 'COMPLETED' or 'CANCELLED'
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
    private List<InvoiceItem> items;

    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}