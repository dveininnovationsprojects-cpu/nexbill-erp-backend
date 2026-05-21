package com.example.billing_backend.repository;

import com.example.billing_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 1. Product ID vachi inventory-ah eduka
    Optional<Inventory> findByProduct_Id(Long productId);

    // 2. Low stock alert-ku (Double type updated)
    List<Inventory> findByAvailableQuantityLessThanEqual(Double reorderLevel);

    // 3. Product name vachi search panna
    @Query("SELECT i FROM Inventory i WHERE i.product.name LIKE %:name%")
    List<Inventory> findByProductNameContaining(@Param("name") String name);

    // 4. Specific quantity theda (Double type updated)
    List<Inventory> findByAvailableQuantity(Double quantity);
}