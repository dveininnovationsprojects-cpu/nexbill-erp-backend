package com.example.billing_backend.service;

import com.example.billing_backend.dto.StockAlertResponse;
import org.springframework.data.domain.Page;

public interface StockAlertService {
    Page<StockAlertResponse> getLowStockAlerts(int page, int size);
    Page<StockAlertResponse> getOutOfStockAlerts(int page, int size);
    Page<StockAlertResponse> getCriticalStockAlerts(int page, int size); // 🔥 New API
    long getLowStockCount();
}