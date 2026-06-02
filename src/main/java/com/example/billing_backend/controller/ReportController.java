package com.example.billing_backend.controller;

import com.example.billing_backend.dto.DashboardSummaryDto;
import com.example.billing_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    // 1. JSON Endpoint for the Visual Dashboard (For Selvamani)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(reportService.getDashboardSummary(startDate, endDate));
    }

    // 2. Automated File Export Endpoint (Generates a downloadable CSV File)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> downloadCsvReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String csvData = reportService.generateSalesCsvReport(startDate, endDate);
        byte[] output = csvData.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report_" + System.currentTimeMillis() + ".csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(output);
    }
}