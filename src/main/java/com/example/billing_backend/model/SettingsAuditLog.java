package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settings_audit_log")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettingsAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String updatedBy; // Admin Email/Username

    @Column(columnDefinition = "TEXT")
    private String changesSummary; // Example: "GST changed from X to Y"

    private LocalDateTime updatedDate;

    @PrePersist
    public void setDate() {
        this.updatedDate = LocalDateTime.now();
    }
}