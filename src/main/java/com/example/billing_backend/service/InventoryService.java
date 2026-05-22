package com.example.billing_backend.service;

import com.example.billing_backend.dto.InventoryResponse; // 🔥 FIX: DTO import panniyachu
import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    void addStock(Long productId, BigDecimal quantity);

    void reduceStock(Long productId, BigDecimal quantity);

    // 🔥 FIX: Return type 'InventoryResponse' ku maathiyachu
    InventoryResponse getInventoryByProductId(Long productId);

    // 🔥 FIX: Return type 'List<InventoryResponse>' ku maathiyachu
    List<InventoryResponse> getLowStockProducts();

    void updateReorderLevel(Long productId, BigDecimal newLevel);
}