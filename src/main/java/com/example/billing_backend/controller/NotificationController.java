package com.example.billing_backend.controller;

import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.User;
import com.example.billing_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    // Both Admin and Cashier hit this same endpoint.
    // The backend automatically filters based on their token!
    @GetMapping("/my-alerts")
    public ResponseEntity<List<Notification>> fetchAlerts(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(notificationService.getMyNotifications(currentUser));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> fetchCount(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(notificationService.getUnreadCount(currentUser));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.readAlert(id);
        return ResponseEntity.ok("Alert read successfully.");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        notificationService.readAllMyAlerts(currentUser);
        return ResponseEntity.ok("All alerts marked as read.");
    }
}