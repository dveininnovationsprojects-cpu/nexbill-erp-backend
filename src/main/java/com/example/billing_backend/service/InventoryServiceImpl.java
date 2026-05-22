package com.example.billing_backend.service;

import com.example.billing_backend.dto.InventoryResponse;
import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.model.Product;
import com.example.billing_backend.repository.InventoryRepository;
import com.example.billing_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    // Helper method to convert Entity to DTO
    private InventoryResponse mapToResponse(Inventory inv) {
        return InventoryResponse.builder()
                .inventoryId(inv.getInventoryId())
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .sku(inv.getProduct().getSku())
                .availableQuantity(inv.getAvailableQuantity())
                .reorderLevel(inv.getReorderLevel())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void addStock(Long productId, BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Quantity must be greater than zero");

        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    return Inventory.builder()
                            .product(product)
                            .availableQuantity(BigDecimal.ZERO)
                            .reorderLevel(BigDecimal.valueOf(10.0))
                            .build();
                });

        if (inv.getProduct().isDeleted())
            throw new RuntimeException("Cannot add stock to a deleted product!");

        inv.setAvailableQuantity(inv.getAvailableQuantity().add(quantity));
        inventoryRepository.save(inv);
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, BigDecimal quantity) {
        // 🔥 FIX: Added Missing Validation
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Quantity must be greater than zero");

        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        // 🔥 FIX: Soft Delete Check
        if (inv.getProduct().isDeleted())
            throw new RuntimeException("Cannot reduce stock for a deleted product!");

        if (inv.getAvailableQuantity().compareTo(quantity) < 0)
            throw new RuntimeException("Insufficient Stock!");

        inv.setAvailableQuantity(inv.getAvailableQuantity().subtract(quantity));
        inventoryRepository.save(inv);
    }

    @Override
    public InventoryResponse getInventoryByProductId(Long productId) {
        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return mapToResponse(inv);
    }

    @Override
    public List<InventoryResponse> getLowStockProducts() {
        return inventoryRepository.findLowStockProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateReorderLevel(Long productId, BigDecimal newLevel) {
        if (newLevel.compareTo(BigDecimal.ZERO) < 0)
            throw new RuntimeException("Reorder level cannot be negative");

        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inv.getProduct().isDeleted())
            throw new RuntimeException("Cannot modify deleted product!");

        inv.setReorderLevel(newLevel);
        inventoryRepository.save(inv);
    }
}