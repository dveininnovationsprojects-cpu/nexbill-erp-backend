
        package com.example.billing_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    // Nullable for direct/custom invoices
    private Long productId;

    @Column(nullable = false)
    private String productName;

    private String sku;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal subtotal;
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal discount;
    private BigDecimal finalTotal;
}