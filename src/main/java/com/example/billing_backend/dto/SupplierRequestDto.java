package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierRequestDto {
    private String companyName;
    private String gstin;
    private String contactPerson;
    private String mobile;
    private String email;
    private String address;
    private String bankDetails;
}