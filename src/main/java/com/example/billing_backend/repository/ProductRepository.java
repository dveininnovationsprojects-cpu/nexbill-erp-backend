package com.example.billing_backend.repository;

import com.example.billing_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByBarcode(String barcode);
    boolean existsBySku(String sku); // Puthusu

    // Delete aagadha products-ah mattum edukka (Soft Delete Filter)
    List<Product> findByIsDeletedFalse();
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);
    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);
}