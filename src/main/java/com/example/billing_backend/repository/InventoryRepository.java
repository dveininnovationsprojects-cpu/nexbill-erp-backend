package com.example.billing_backend.repository;

import com.example.billing_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_Id(Long productId);

    // 🔥 FIX: 'isDeleted' nu theliva maathiyachu!
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel AND i.product.isDeleted = false")
    List<Inventory> findLowStockProducts();

    @Query("SELECT i FROM Inventory i WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :name, '%')) AND i.product.isDeleted = false")
    List<Inventory> findByProductNameContaining(@Param("name") String name);

    List<Inventory> findByAvailableQuantity(BigDecimal quantity);
}