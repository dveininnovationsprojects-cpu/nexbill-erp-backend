package com.example.billing_backend.service;

import com.example.billing_backend.dto.StockAlertResponse;
import com.example.billing_backend.model.Inventory;
import com.example.billing_backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StockAlertServiceImpl implements StockAlertService {

    private final InventoryRepository inventoryRepository;

    private StockAlertResponse mapToAlert(Inventory inv) {
        BigDecimal qty = inv.getAvailableQuantity();
        BigDecimal reorder = inv.getReorderLevel();

        // 🔥 Enterprise Fix: Precise Status Logic
        String alertStatus;
        if (qty.compareTo(BigDecimal.ZERO) == 0) {
            alertStatus = "OUT OF STOCK";
        } else if (qty.compareTo(BigDecimal.valueOf(2)) <= 0) {
            alertStatus = "CRITICAL STOCK";
        } else if (qty.compareTo(reorder) <= 0) {
            alertStatus = "LOW STOCK";
        } else {
            alertStatus = "SAFE";
        }

        return StockAlertResponse.builder()
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .sku(inv.getProduct().getSku())
                .availableQuantity(qty)
                .reorderLevel(reorder)
                .status(alertStatus)
                .updatedAt(inv.getUpdatedAt()) // 🔥 Added Timestamp
                .build();
    }

    @Override
    public Page<StockAlertResponse> getLowStockAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryRepository.findLowStockProducts(pageable).map(this::mapToAlert);
    }

    @Override
    public Page<StockAlertResponse> getOutOfStockAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryRepository.findOutOfStockProducts(pageable).map(this::mapToAlert);
    }

    @Override
    public Page<StockAlertResponse> getCriticalStockAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryRepository.findCriticalStockProducts(pageable).map(this::mapToAlert);
    }

    @Override
    public long getLowStockCount() {
        return inventoryRepository.countLowStockAlerts();
    }
}