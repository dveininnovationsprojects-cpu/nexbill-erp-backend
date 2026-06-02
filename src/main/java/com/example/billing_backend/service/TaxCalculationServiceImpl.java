package com.example.billing_backend.service;

import com.example.billing_backend.dto.GstReportResponse;
import com.example.billing_backend.dto.TaxCalculationRequest;
import com.example.billing_backend.dto.TaxSummaryResponse;
import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxCalculationServiceImpl implements TaxCalculationService {

    private final InvoiceRepository invoiceRepository;

    private final List<BigDecimal> validGstRates = Arrays.asList(
            BigDecimal.ZERO,
            new BigDecimal("5.0"),
            new BigDecimal("12.0"),
            new BigDecimal("18.0"),
            new BigDecimal("28.0")
    );

    @Override
    public TaxSummaryResponse calculateTax(TaxCalculationRequest request) {
        BigDecimal subtotal = request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal gstPercentage = request.getGstPercentage() != null ? request.getGstPercentage() : BigDecimal.ZERO;

        // 🔥 Issue 4 Fix: Subtotal negative validation
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Subtotal cannot be negative!");
        }

        boolean isValidGst = validGstRates.stream().anyMatch(rate -> rate.compareTo(gstPercentage) == 0);
        if (!isValidGst) {
            throw new RuntimeException("Invalid GST Percentage! Allowed rates are: 0, 5, 12, 18, 28");
        }

        if (gstPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("GST cannot be negative!");
        }

        if (discount.compareTo(subtotal) > 0) {
            throw new RuntimeException("Discount amount cannot exceed the subtotal!");
        }

        // 🔥 Issue 1 Fix: GST calculated on Taxable Amount (Subtotal - Discount)
        BigDecimal taxableAmount = subtotal.subtract(discount);

        BigDecimal gstAmount = taxableAmount.multiply(gstPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = taxableAmount.add(gstAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return TaxSummaryResponse.builder()
                .subtotal(subtotal)
                .discount(discount)
                .gstPercentage(gstPercentage)
                .gstAmount(gstAmount)
                .grandTotal(grandTotal)
                .build();
    }

    @Override
    public TaxSummaryResponse getTaxSummaryForBill(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        // 🔥 Future Proofing (Issue 5 concept): Check if bill is Cancelled
        // if(invoice.getStatus().equals("CANCELLED")) { throw new RuntimeException("Cannot fetch tax for cancelled bill"); }

        return TaxSummaryResponse.builder()
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscountTotal())
                // gstPercentage is omitted here, @JsonInclude(NON_NULL) will hide it in response
                .gstAmount(invoice.getGstTotal())
                .grandTotal(invoice.getGrandTotal())
                .build();
    }

    @Override
    public GstReportResponse getGstReport() {
        // 🔥 Issue 3 Fix: Zero Java Looping! Pure Database Aggregation Power!
        long totalBills = invoiceRepository.countTotalInvoices();
        BigDecimal totalSales = invoiceRepository.sumTotalSubtotal();
        BigDecimal totalDiscount = invoiceRepository.sumTotalDiscount();
        BigDecimal totalGst = invoiceRepository.sumTotalGst();
        BigDecimal totalRevenue = invoiceRepository.sumTotalRevenue();

        return GstReportResponse.builder()
                .totalBillsGenerated(totalBills)
                .totalSalesSubtotal(totalSales)
                .totalDiscountGiven(totalDiscount)
                .totalGstCollected(totalGst)
                .totalRevenue(totalRevenue)
                .build();
    }
}