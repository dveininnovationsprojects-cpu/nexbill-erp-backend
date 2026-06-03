package com.example.billing_backend.model;

public enum NotificationType {
    NEW_USER_REGISTRATION, // Cashier applies for account
    PROFILE_APPROVED,      // Admin approves the cashier
    PROFILE_REJECTED,      // Admin rejects the cashier
    LOW_STOCK_ALERT,       // Inventory drops below threshold
    DEAD_STOCK_WARNING,    // Items not sold for a long time
    HIGH_VALUE_SALES,      // Heavy billing done at counter
    DRAWER_MISMATCH,       // Cash mismatch during shift close
    SYSTEM_UPDATE          // General blasts
}