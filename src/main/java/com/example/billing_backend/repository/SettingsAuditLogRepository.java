package com.example.billing_backend.repository;

import com.example.billing_backend.model.SettingsAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsAuditLogRepository extends JpaRepository<SettingsAuditLog, Long> {
}