package com.example.billing_backend.service;

import com.example.billing_backend.enums.ActionType;
import com.example.billing_backend.enums.ModuleName;
import com.example.billing_backend.model.SystemLog;

import java.time.LocalDate;
import java.util.List;

// 🔥 Maathiyachi! Idhu ippo thelivaana INTERFACE
public interface SystemLogService {
    void logAction(String username, String role, ModuleName module, ActionType action, String description, String referenceId, String ipAddress);

    List<SystemLog> getAllLogs();
    List<SystemLog> getLogsByModule(ModuleName moduleName);
    List<SystemLog> getLogsByUser(String username);
    List<SystemLog> getLogsByDate(LocalDate startDate, LocalDate endDate);
}