
        package com.example.billing_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class BillRequest {
    private String paymentMethod;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String customerGstNo;
    private Long customerId;
    private String dueDate;
    private Double discount;
    private String notes;
    private List<BillItemRequest> items;
    private String status;
}