package com.example.billing_backend.service;

public interface InvoicePdfService {
    byte[] generateInvoicePdf(String invoiceNumber);
}