package com.example.billing_backend.service;

import com.example.billing_backend.dto.GstReportResponse;
import com.example.billing_backend.dto.TaxCalculationRequest;
import com.example.billing_backend.dto.TaxSummaryResponse;

public interface TaxCalculationService {
    TaxSummaryResponse calculateTax(TaxCalculationRequest request);
    TaxSummaryResponse getTaxSummaryForBill(String invoiceNumber);
    GstReportResponse getGstReport();
}