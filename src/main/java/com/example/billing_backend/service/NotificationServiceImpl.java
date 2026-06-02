package com.example.billing_backend.service;

import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.NotificationType;
import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 1. CASHIER REGISTER -> ADMIN GETS ALERT
    @Override
    public void triggerNewRegistrationAlertToAdmins(User newCashier) {
        Notification alert = Notification.builder()
                .title("New Cashier Registration")
                .message("User " + newCashier.getName() + " (" + newCashier.getEmail() + ") has registered and is pending approval.")
                .type(NotificationType.NEW_USER_REGISTRATION)
                .targetedRole(Role.ADMIN) // Sent to ALL admins
                .targetedUser(null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(alert);
    }

    // 2. ADMIN APPROVES -> CASHIER GETS ALERT
    @Override
    public void triggerApprovalAlertToCashier(User approvedCashier) {
        Notification alert = Notification.builder()
                .title("Profile Approved!")
                .message("Hello " + approvedCashier.getName() + ", your cashier profile has been approved. You can now login and generate bills.")
                .type(NotificationType.PROFILE_APPROVED)
                .targetedRole(null)
                .targetedUser(approvedCashier) // Sent explicitly to THIS cashier only
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(alert);
    }

    // 3. LOW STOCK (INVENTORY MODULE) -> ADMIN GETS ALERT
    @Override
    public void triggerLowStockAdminAlert(String productName, int currentStock) {
        Notification alert = Notification.builder()
                .title("Critical Low Stock Alert")
                .message("Product '" + productName + "' has dropped to " + currentStock + " units. Please restock immediately.")
                .type(NotificationType.LOW_STOCK_ALERT)
                .targetedRole(Role.ADMIN)
                .targetedUser(null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(alert);
    }

    // 4. BIG SALE (ORDER MODULE) -> ADMIN GETS ALERT
    @Override
    public void triggerHighValueSaleAlert(String cashierName, String invoiceNo, double amount) {
        Notification alert = Notification.builder()
                .title("High Value Transaction")
                .message("Cashier " + cashierName + " processed a large invoice (" + invoiceNo + ") for ₹" + amount)
                .type(NotificationType.HIGH_VALUE_SALES)
                .targetedRole(Role.ADMIN)
                .targetedUser(null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(alert);
    }

    @Override
    public List<Notification> getMyNotifications(User user) {
        return notificationRepository.fetchMyNotifications(user.getRole(), user.getId());
    }

    @Override
    public Long getUnreadCount(User user) {
        return notificationRepository.countMyUnreadAlerts(user.getRole(), user.getId());
    }

    @Override
    public void readAlert(Long id) {
        notificationRepository.markSingleAsRead(id);
    }

    @Override
    public void readAllMyAlerts(User user) {
        notificationRepository.markAllAsReadForMyProfile(user.getRole(), user.getId());
    }
}