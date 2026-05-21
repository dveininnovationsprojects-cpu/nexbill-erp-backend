package com.example.billing_backend.repository;

import com.example.billing_backend.model.Supplier;
import com.example.billing_backend.model.SupplierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findByGstin(String gstin);
    List<Supplier> findByStatus(SupplierStatus status);
}