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

    // 3. View All Bills (For Admin History)
    List<Invoice> getAllBills();

    // 4. View Cashier Own Bills
    List<Invoice> getInvoicesByCashier(String cashierId);

    // =========================================================
    // 🔥 NEW ADDITION: CANCEL INVOICE (Soft Delete & Restock)
    // =========================================================
    String cancelInvoice(String invoiceNumber, String cancelledBy);
}