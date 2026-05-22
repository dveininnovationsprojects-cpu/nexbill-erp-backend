package com.example.billing_backend.repository;

import com.example.billing_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByBarcode(String barcode);
    boolean existsBySku(String sku);
    Optional<Product> findBySku(String sku);

    List<Product> findByIsDeletedFalse();
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
    List<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId);

    // 🔥 ENTERPRISE FIX: Multi-field Search Engine for Cashier
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);
}