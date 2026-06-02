package com.example.billing_backend.controller;

import com.example.billing_backend.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class InvoicePdfController {

    private final InvoicePdfService invoicePdfService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    @GetMapping("/download/{invoiceNumber}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String invoiceNumber) {

        // 1. Get PDF file as byte array
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoiceNumber);

        // 2. Setup Headers to trigger File Download in Browser/Postman
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", invoiceNumber + ".pdf");
        headers.setContentLength(pdfBytes.length);

        // 3. Return the file
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}