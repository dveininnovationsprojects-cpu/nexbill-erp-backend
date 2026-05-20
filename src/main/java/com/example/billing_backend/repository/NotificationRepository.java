package com.example.billing_backend.repository;

import com.example.billing_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
}