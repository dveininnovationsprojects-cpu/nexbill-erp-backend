package com.example.billing_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // TARGETING LOGIC: Yarukku anuppanum?
    @Enumerated(EnumType.STRING)
    private Role targetedRole; // ROLE_ADMIN (or) ROLE_CASHIER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targeted_user_id")
    private User targetedUser; // Null if blasting to ALL admins

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}