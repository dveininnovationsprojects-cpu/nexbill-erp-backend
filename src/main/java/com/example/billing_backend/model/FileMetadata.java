package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileName; // Stored unique name (UUID)

    @Column(nullable = false)
    private String originalName; // User's actual file name

    @Column(nullable = false)
    private String fileType; // image/png, application/pdf, etc.

    private long fileSize; // Size in bytes

    @Column(nullable = false)
    private String filePath; // Actual path on server

    @Column(nullable = false)
    private String category; // PRODUCT_IMAGE, INVOICE, etc.

    @Column(nullable = false)
    private String uploadedBy;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void setDate() {
        this.uploadedAt = LocalDateTime.now();
    }
}