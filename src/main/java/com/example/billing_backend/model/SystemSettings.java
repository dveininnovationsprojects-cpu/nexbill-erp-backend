package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SystemSettings {

    @Id
    private Long id; // Always 1 (Single Row Architecture)

    @Column(nullable = false)
    private String companyName;

    private String companyAddress;
    private String companyPhone;
    private String companyEmail;

    @Column(nullable = false)
    private String gstNumber;

    @Column(nullable = false)
    private String invoicePrefix;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private int defaultReorderLevel;

    private String logoUrl;

    private LocalDateTime updatedAt;
}