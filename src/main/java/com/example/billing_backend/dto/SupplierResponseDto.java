package com.example.billing_backend.dto;

import com.example.billing_backend.model.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponseDto {
    private Integer id;
    private String companyName;
    private String gstin;
    private String contactPerson;
    private String mobile;
    private String email;
    private String address;
    private String bankDetails;
    private SupplierStatus status;
    private Double totalPurchasedAmount;
    private Double totalPaidAmount;
    private Double outstandingBalance;
}