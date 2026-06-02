package com.example.billing_backend.repository;

import com.example.billing_backend.model.Notification;
import com.example.billing_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> { // Note: Keep this Long if Notification ID is Long.

    // FIX: Changed Long userId to Integer userId
    @Query("SELECT n FROM Notification n WHERE n.targetedRole = :role OR n.targetedUser.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> fetchMyNotifications(@Param("role") Role role, @Param("userId") Integer userId);

    // FIX: Changed Long userId to Integer userId
    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.targetedRole = :role OR n.targetedUser.id = :userId) AND n.isRead = false")
    Long countMyUnreadAlerts(@Param("role") Role role, @Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markSingleAsRead(@Param("id") Long id); // Notification ID remains Long

    // FIX: Changed Long userId to Integer userId
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetedRole = :role OR n.targetedUser.id = :userId")
    void markAllAsReadForMyProfile(@Param("role") Role role, @Param("userId") Integer userId);
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :id")
    void deleteSingleAlert(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.targetedRole = :role OR n.targetedUser.id = :userId")
    void deleteAllMyAlerts(@Param("role") Role role, @Param("userId") Integer userId);
}