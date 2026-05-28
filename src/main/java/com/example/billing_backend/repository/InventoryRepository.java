package com.example.billing_backend.repository;

import com.example.billing_backend.dto.CategoryValuationDto;
import com.example.billing_backend.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_Id(Long productId);

    @Query("SELECT i FROM Inventory i WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :name, '%')) AND i.product.isDeleted = false")
    List<Inventory> findByProductNameContaining(@Param("name") String name);

    // ==========================================
    // 🔥 ENTERPRISE STOCK ALERT QUERIES (Fixed)
    // ==========================================

    @Query("""
        SELECT i FROM Inventory i 
        WHERE i.availableQuantity <= i.reorderLevel 
        AND i.availableQuantity > 0 
        AND i.product.isDeleted = false 
        ORDER BY i.availableQuantity ASC
    """)
    Page<Inventory> findLowStockProducts(Pageable pageable);

    @Query("""
        SELECT i FROM Inventory i 
        WHERE i.availableQuantity = 0 
        AND i.product.isDeleted = false 
        ORDER BY i.updatedAt DESC
    """)
    Page<Inventory> findOutOfStockProducts(Pageable pageable);

    @Query("""
        SELECT i FROM Inventory i 
        WHERE i.availableQuantity <= 2 
        AND i.availableQuantity > 0 
        AND i.product.isDeleted = false 
        ORDER BY i.availableQuantity ASC
    """)
    Page<Inventory> findCriticalStockProducts(Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel AND i.product.isDeleted = false")
    long countLowStockAlerts();
    // Out of Stock Counter
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= 0")
    Long countOutOfStockItems();

    // Category-wise Valuation Matrix
    @Query("SELECT new com.example.billing_backend.dto.CategoryValuationDto(c.name, SUM(i.availableQuantity * p.purchasePrice)) FROM Inventory i JOIN i.product p JOIN p.category c GROUP BY c.name")
    List<CategoryValuationDto> getCategoryValuations();
}