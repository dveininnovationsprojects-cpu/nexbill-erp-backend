package com.example.billing_backend.service;

import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.model.Product;
import com.example.billing_backend.repository.InventoryRepository;
import com.example.billing_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository; // PUDHUSA ADD PANNIRUKKOM

    @Override
    @Transactional
    public void addStock(Long productId, Double quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        // SMART LOGIC: Inventory irundha edukkum, illana DB-la irundhu Product-ah thedi pudhusa create pannum!
        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Error: Product ID " + productId + " does not exist in the Product table!"));

                    return Inventory.builder()
                            .product(product)
                            .availableQuantity(0.0) // Initial empty stock
                            .reorderLevel(10.0) // Default alert level
                            .build();
                });

        inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inv);
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, Double quantity) {
        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for Product ID: " + productId));

        if (inv.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient Stock! Available: " + inv.getAvailableQuantity());
        }

        inv.setAvailableQuantity(inv.getAvailableQuantity() - quantity);
        inv.setUpdatedAt(LocalDateTime.now());

        if (inv.getAvailableQuantity() <= inv.getReorderLevel()) {
            System.err.println("🚨 LOW STOCK ALERT: " + inv.getProduct().getName() + " left: " + inv.getAvailableQuantity());
        }

        inventoryRepository.save(inv);
    }

    @Override
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for Product ID: " + productId));
    }

    @Override
    public List<Inventory> getLowStockProducts(Double threshold) {
        return inventoryRepository.findByAvailableQuantityLessThanEqual(threshold);
    }

    @Override
    @Transactional
    public void updateReorderLevel(Long productId, Double newLevel) {
        if (newLevel < 0) {
            throw new RuntimeException("Reorder level cannot be negative");
        }

        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for Product ID: " + productId));

        inv.setReorderLevel(newLevel);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inv);
    }
}