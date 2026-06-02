package com.example.billing_backend.service;

import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.User;
import java.util.List;

public interface NotificationService {
    // Dedicated Triggers
    void triggerNewRegistrationAlertToAdmins(User newCashier);
    void triggerApprovalAlertToCashier(User approvedCashier);
    void triggerLowStockAdminAlert(String productName, int currentStock);
    void triggerHighValueSaleAlert(String cashierName, String invoiceNo, double amount);

    // 🔥 NEW MISSING METHODS ADDED HERE
    void blastAnnouncementToCashiers(String title, String message);
    void triggerLowStockAlert(String productName, double currentStock);
    void sendDirectNotification(User targetedUser, String title, String message);

    // Fetchers
    List<Notification> getMyNotifications(User user);
    Long getUnreadCount(User user);
    void readAlert(Long id);
    void readAllMyAlerts(User user);
}