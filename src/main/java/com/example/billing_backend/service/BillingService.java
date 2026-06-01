package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.model.Invoice;
import java.util.List;

public interface BillingService {
    BillResponse checkout(String cashierId, BillRequest request);

    // Indha rendu line interface-la irukkanum
    Invoice getBillByInvoiceNumber(String invoiceNumber);
    List<Invoice> getAllBills();
}