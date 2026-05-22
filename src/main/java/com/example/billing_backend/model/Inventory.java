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

    // 🔥 ENTERPRISE FIX: Optimistic Locking (Prevents 2 cashiers billing same item at exactly same millisecond)
    @Version
    private Long version;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void setUpdatedAt() { this.updatedAt = LocalDateTime.now(); }
}