package com.example.billing_backend.service;

import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.repository.InvoiceRepository;
import com.example.billing_backend.utils.PdfGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final InvoiceRepository invoiceRepository;

    // 🔥 Issue 6 Fix: Fetching from application.properties
    @Value("${company.name}")
    private String companyName;

    @Value("${company.address}")
    private String companyAddress;

    @Value("${company.phone}")
    private String companyPhone;

    @Value("${company.gstin}")
    private String companyGstin;

    @Override
    public byte[] generateInvoicePdf(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        // 🔥 Issue 2 Fix: Cancelled Bill Validation
        if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new RuntimeException("Cannot generate PDF! This invoice has been CANCELLED.");
        }

        // 🔥 Issue 7 Fix: Empty Items Validation
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new RuntimeException("Invalid Invoice! No products found in this bill.");
        }

        // Passing the Dynamic Company Info to the Util
        return PdfGeneratorUtil.generateInvoicePdf(invoice, companyName, companyAddress, companyPhone, companyGstin);
    }
}