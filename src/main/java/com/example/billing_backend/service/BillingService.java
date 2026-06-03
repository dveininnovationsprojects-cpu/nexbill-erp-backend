package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.model.Invoice;
import java.util.List;

public interface BillingService {

    // 1. Generate Bill
    BillResponse checkout(String cashierId, BillRequest request);

    // 2. View Specific Bill (For Reprint / PDF)
    Invoice getBillByInvoiceNumber(String invoiceNumber);

    // =========================================================
    // 🔥 FRONTEND ISSUE 5 FIX: History Access (Replaces getAllBills)
    // =========================================================
    List<Invoice> getAllBillsForUser(String userEmail, String role);

    // =========================================================
    // 🔥 CANCEL INVOICE (Soft Delete & Restock)
    // =========================================================
    String cancelInvoice(String invoiceNumber, String cancelledBy);
}