package com.example.billing_backend.repository;

import com.example.billing_backend.dto.CategoryValuationDto;
import com.example.billing_backend.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 1. Normal Inventory Fetch
    Optional<Inventory> findByProduct_Id(Long productId);

    // 2. 🔥 Anti-Race Condition Lock for Billing Module
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProduct_IdForUpdate(@Param("productId") Long productId);

    // =======================================================
    // 🔥 STOCK ALERT QUERIES (These were missing earlier!)
    // Returns Page<Inventory> so that .map() works perfectly in StockAlertService
    // =======================================================

    // 3. Low Stock: Qty <= Reorder Level
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel")
    Page<Inventory> findLowStockProducts(Pageable pageable);

    // 4. Out of Stock: Qty = 0
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity = 0")
    Page<Inventory> findOutOfStockProducts(Pageable pageable);

    // 5. Critical Stock: Qty between 1 and 2
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity > 0 AND i.availableQuantity <= 2")
    Page<Inventory> findCriticalStockProducts(Pageable pageable);

    // 6. Count Low Stock for Dashboard
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel")
    long countLowStockAlerts();
    // Out of Stock Counter
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= 0")
    Long countOutOfStockItems();

    // Category-wise Valuation Matrix
    @Query("SELECT new com.example.billing_backend.dto.CategoryValuationDto(c.name, SUM(i.availableQuantity * p.purchasePrice)) FROM Inventory i JOIN i.product p JOIN p.category c GROUP BY c.name")
    List<CategoryValuationDto> getCategoryValuations();
}