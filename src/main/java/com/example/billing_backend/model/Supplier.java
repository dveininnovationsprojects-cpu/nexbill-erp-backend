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
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String gstin;

    private String contactPerson;

    @Column(nullable = false)
    private String mobile;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String bankDetails;

    @Enumerated(EnumType.STRING)
    private SupplierStatus status;

    private Double totalPurchasedAmount;
    private Double totalPaidAmount;
    private Double outstandingBalance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = SupplierStatus.ACTIVE;
        if (totalPurchasedAmount == null) totalPurchasedAmount = 0.0;
        if (totalPaidAmount == null) totalPaidAmount = 0.0;
        if (outstandingBalance == null) outstandingBalance = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}