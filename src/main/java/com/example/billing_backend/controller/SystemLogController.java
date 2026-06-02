package com.example.billing_backend.controller;

import com.example.billing_backend.enums.ModuleName;
import com.example.billing_backend.model.SystemLog;
import com.example.billing_backend.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')") // Admin only Security
public class SystemLogController {

    private final SystemLogService systemLogService;

    @GetMapping
    public ResponseEntity<List<SystemLog>> getAllLogs() {
        return ResponseEntity.ok(systemLogService.getAllLogs());
    }

    @GetMapping("/module/{moduleName}")
    public ResponseEntity<List<SystemLog>> getLogsByModule(@PathVariable ModuleName moduleName) {
        return ResponseEntity.ok(systemLogService.getLogsByModule(moduleName));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<SystemLog>> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(systemLogService.getLogsByUser(username));
    }

    @GetMapping("/date")
    public ResponseEntity<List<SystemLog>> getLogsByDate(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(systemLogService.getLogsByDate(fromDate, toDate));
    }
}