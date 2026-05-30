package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;

public interface BillingService {
    BillResponse checkout(String cashierId, BillRequest request);
}