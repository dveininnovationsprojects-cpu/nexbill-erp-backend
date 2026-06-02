package com.example.billing_backend.repository;

import com.example.billing_backend.enums.ModuleName;
import com.example.billing_backend.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    List<SystemLog> findAllByOrderByCreatedAtDesc();

    List<SystemLog> findByModuleNameOrderByCreatedAtDesc(ModuleName moduleName);

    List<SystemLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<SystemLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}