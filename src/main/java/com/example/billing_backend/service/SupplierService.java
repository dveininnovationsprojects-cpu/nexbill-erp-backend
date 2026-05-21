package com.example.billing_backend.service;

import com.example.billing_backend.dto.SupplierRequestDto;
import com.example.billing_backend.dto.SupplierResponseDto;
import java.util.List;

public interface SupplierService {
    SupplierResponseDto addSupplier(SupplierRequestDto request);
    SupplierResponseDto updateSupplier(Integer id, SupplierRequestDto request);
    String toggleSupplierStatus(Integer id);
    SupplierResponseDto getSupplierById(Integer id);
    List<SupplierResponseDto> getAllSuppliers();
    List<SupplierResponseDto> getActiveSuppliers();
}