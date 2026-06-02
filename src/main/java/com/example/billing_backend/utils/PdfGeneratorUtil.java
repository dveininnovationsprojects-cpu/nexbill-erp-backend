package com.example.billing_backend.utils;

import com.example.billing_backend.model.Invoice;
import com.example.billing_backend.model.InvoiceItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class PdfGeneratorUtil {

    public static byte[] generateInvoicePdf(Invoice invoice, String compName, String compAddr, String compPhone, String compGst) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // 🔥 Issue 4 Fix: Professional Currency Formatter
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

            // 1. Title
            Paragraph title = new Paragraph("TAX INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. Info Section (3 Columns: Company, Customer, Invoice Details)
            PdfPTable infoTable = new PdfPTable(3);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f, 1f});

            // Company Details
            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph(compName, headFont));
            companyCell.addElement(new Paragraph(compAddr, normalFont));
            companyCell.addElement(new Paragraph("Phone: " + compPhone, normalFont));
            companyCell.addElement(new Paragraph("GSTIN: " + compGst, normalFont));
            infoTable.addCell(companyCell);

            // 🔥 Issue 1 Fix: Customer Details
            PdfPCell customerCell = new PdfPCell();
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.addElement(new Paragraph("Billed To:", boldFont));
            String custName = invoice.getCustomerName() != null ? invoice.getCustomerName() : "Cash Customer";
            String custPhone = invoice.getCustomerPhone() != null ? invoice.getCustomerPhone() : "N/A";
            customerCell.addElement(new Paragraph(custName, normalFont));
            customerCell.addElement(new Paragraph("Phone: " + custPhone, normalFont));
            infoTable.addCell(customerCell);

            // Invoice Details
            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBorder(Rectangle.NO_BORDER);
            invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            invoiceCell.addElement(new Paragraph("Invoice No: " + invoice.getInvoiceNumber(), boldFont));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            String dateStr = invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(formatter) : "N/A";
            invoiceCell.addElement(new Paragraph("Date: " + dateStr, normalFont));
            invoiceCell.addElement(new Paragraph("Payment: " + invoice.getPaymentMethod(), normalFont));
            infoTable.addCell(invoiceCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // 3. Products Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 1.5f, 1f, 1.5f, 1.5f});
            table.setSpacingBefore(10);

            String[] headers = {"Product Name", "Qty", "Price", "GST%", "GST Amt", "Total"};
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, boldFont));
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hCell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(hCell);
            }

            for (InvoiceItem item : invoice.getItems()) {
                table.addCell(createCell(item.getProductName(), normalFont, Element.ALIGN_LEFT));
                table.addCell(createCell(item.getQuantity().toString(), normalFont, Element.ALIGN_CENTER));
                table.addCell(createCell(currencyFormat.format(item.getUnitPrice()), normalFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(item.getGstPercentage() + "%", normalFont, Element.ALIGN_CENTER));
                table.addCell(createCell(currencyFormat.format(item.getGstAmount()), normalFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(currencyFormat.format(item.getFinalTotal()), normalFont, Element.ALIGN_RIGHT));
            }
            document.add(table);

            // 4. Summary Box
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(15);

            // 🔥 Issue 3 Fix: Null Protection (Optional.ofNullable)
            BigDecimal subtotal = Optional.ofNullable(invoice.getSubtotal()).orElse(BigDecimal.ZERO);
            BigDecimal discount = Optional.ofNullable(invoice.getDiscountTotal()).orElse(BigDecimal.ZERO);
            BigDecimal gstTotal = Optional.ofNullable(invoice.getGstTotal()).orElse(BigDecimal.ZERO);
            BigDecimal grandTotal = Optional.ofNullable(invoice.getGrandTotal()).orElse(BigDecimal.ZERO);

            addSummaryRow(summaryTable, "Subtotal:", currencyFormat.format(subtotal), normalFont);
            addSummaryRow(summaryTable, "Discount:", currencyFormat.format(discount), normalFont);
            addSummaryRow(summaryTable, "Total GST:", currencyFormat.format(gstTotal), normalFont);
            addSummaryRow(summaryTable, "Grand Total:", currencyFormat.format(grandTotal), boldFont);

            document.add(summaryTable);

            // 5. Footer
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Thank You for shopping with " + compName + "!\nVisit Again.", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }

        return out.toByteArray();
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, font));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}