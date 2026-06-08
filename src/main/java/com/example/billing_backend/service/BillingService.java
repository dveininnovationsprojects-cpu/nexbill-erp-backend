
package com.example.billing_backend.service;

import com.example.billing_backend.dto.BillRequest;
import com.example.billing_backend.dto.BillResponse;
import com.example.billing_backend.model.Invoice;

import java.util.List;

public interface BillingService {
    BillResponse checkout(String cashierId, BillRequest request);
    BillResponse createDirectInvoice(String cashierId, BillRequest request);
    Invoice getBillByInvoiceNumber(String invoiceNumber);
    List<Invoice> getAllBillsForUser(String userEmail, String role);
    String cancelInvoice(String invoiceNumber, String cancelledBy);
    String markAsPaid(String invoiceNumber, String updatedBy);
}