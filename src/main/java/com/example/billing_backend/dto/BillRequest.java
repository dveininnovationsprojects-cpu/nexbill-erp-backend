package com.example.billing_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class BillRequest {
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // 🔥 New Fields Added Below
    private String customerAddress;
    private String customerGstNo;
    private String dueDate;
    private Double discount;
    private String notes;
    private List<BillItemRequest> items;
}