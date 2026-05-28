package com.example.billing_backend.repository;

import com.example.billing_backend.dto.CustomerInsightDto;
import com.example.billing_backend.model.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByMobile(String mobile);
    // Top VIP Customers
    @Query("SELECT new com.example.billing_backend.dto.CustomerInsightDto(c.name, c.mobile, c.totalSpentAmount) FROM Customer c ORDER BY c.totalSpentAmount DESC")
    List<CustomerInsightDto> findTopSpenders(Pageable pageable);

    // New Customer Acquisition Graph
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Long countNewAcquisitions(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}