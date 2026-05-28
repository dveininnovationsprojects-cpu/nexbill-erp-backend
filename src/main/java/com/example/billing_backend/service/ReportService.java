package com.example.billing_backend.service;

import com.example.billing_backend.dto.DashboardSummaryDto;
import java.time.LocalDateTime;

public interface ReportService {
    DashboardSummaryDto getDashboardSummary(LocalDateTime startDate, LocalDateTime endDate);
    String generateSalesCsvReport(LocalDateTime startDate, LocalDateTime endDate);
}