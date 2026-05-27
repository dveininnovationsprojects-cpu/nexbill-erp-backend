package com.example.billing_backend.service;

import com.example.billing_backend.dto.InventoryResponse;
import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    void addStock(Long productId, BigDecimal quantity);
    void reduceStock(Long productId, BigDecimal quantity);

    InventoryResponse getInventoryByProductId(Long productId);
    List<InventoryResponse> getLowStockProducts();

    void updateReorderLevel(Long productId, BigDecimal newLevel);
}