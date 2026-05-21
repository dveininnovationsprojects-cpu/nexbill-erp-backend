package com.example.billing_backend.service;

import com.example.billing_backend.model.Inventory;
import java.util.List;

public interface InventoryService {
    void addStock(Long productId, Double quantity);
    void reduceStock(Long productId, Double quantity);
    Inventory getInventoryByProductId(Long productId);
    List<Inventory> getLowStockProducts(Double threshold);
    void updateReorderLevel(Long productId, Double newLevel);
}