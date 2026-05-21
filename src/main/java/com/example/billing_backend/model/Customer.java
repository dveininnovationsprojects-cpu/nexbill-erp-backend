package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 15)
    private String mobile;

    private String email;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier;

    private Double totalSpentAmount;
    private Double creditLimit;
    private Double outstandingDebt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (tier == null) tier = CustomerTier.REGULAR;
        if (totalSpentAmount == null) totalSpentAmount = 0.0;
        if (creditLimit == null) creditLimit = 0.0;
        if (outstandingDebt == null) outstandingDebt = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}