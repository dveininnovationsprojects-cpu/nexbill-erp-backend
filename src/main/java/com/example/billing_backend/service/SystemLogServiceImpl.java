package com.example.billing_backend.service;

import com.example.billing_backend.enums.ActionType;
import com.example.billing_backend.enums.ModuleName;
import com.example.billing_backend.model.SystemLog;
import com.example.billing_backend.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Override
    public void logAction(String username, String role, ModuleName module, ActionType action, String description, String referenceId, String ipAddress) {
        SystemLog log = SystemLog.builder()
                .username(username)
                .role(role)
                .moduleName(module)
                .actionType(action)
                .description(description)
                .referenceId(referenceId)
                .ipAddress(ipAddress != null ? ipAddress : "SYSTEM")
                .build();

        systemLogRepository.save(log);
    }

    @Override
    public List<SystemLog> getAllLogs() {
        return systemLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<SystemLog> getLogsByModule(ModuleName moduleName) {
        return systemLogRepository.findByModuleNameOrderByCreatedAtDesc(moduleName);
    }

    @Override
    public List<SystemLog> getLogsByUser(String username) {
        return systemLogRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public List<SystemLog> getLogsByDate(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return systemLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}