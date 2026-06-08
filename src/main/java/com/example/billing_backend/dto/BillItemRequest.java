
        package com.example.billing_backend.dto;

import lombok.Data;

@Data
public class BillItemRequest {
    private Long productId; // optional — null for custom items
    private String name;
    private Integer qty;
    private Double rate;
    private Double gst;
}