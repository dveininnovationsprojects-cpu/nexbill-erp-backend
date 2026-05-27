package com.example.billing_backend.repository;

import com.example.billing_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Database level duplicate check query
    boolean existsByName(String name);
}