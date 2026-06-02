package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private BigDecimal availableQuantity;
    private BigDecimal reorderLevel;


    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean lowStockAlert = false;

    @Version
    private Long version;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void setUpdatedAt() { this.updatedAt = LocalDateTime.now(); }
}