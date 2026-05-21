package com.example.billing_backend.service;

import com.example.billing_backend.dto.SupplierRequestDto;
import com.example.billing_backend.dto.SupplierResponseDto;
import com.example.billing_backend.model.Supplier;
import com.example.billing_backend.model.SupplierStatus;
import com.example.billing_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public SupplierResponseDto addSupplier(SupplierRequestDto request) {
        if (supplierRepository.findByGstin(request.getGstin()).isPresent()) {
            throw new RuntimeException("Supplier with this GSTIN already exists!");
        }

        Supplier supplier = Supplier.builder()
                .companyName(request.getCompanyName())
                .gstin(request.getGstin())
                .contactPerson(request.getContactPerson())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .address(request.getAddress())
                .bankDetails(request.getBankDetails())
                .build();

        Supplier savedSupplier = supplierRepository.save(supplier);
        return mapToResponseDto(savedSupplier);
    }

    @Override
    public SupplierResponseDto updateSupplier(Integer id, SupplierRequestDto request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setCompanyName(request.getCompanyName());
        supplier.setGstin(request.getGstin());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setMobile(request.getMobile());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setBankDetails(request.getBankDetails());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return mapToResponseDto(updatedSupplier);
    }

    @Override
    public String toggleSupplierStatus(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        if (supplier.getStatus() == SupplierStatus.ACTIVE) {
            supplier.setStatus(SupplierStatus.SUSPENDED);
        } else {
            supplier.setStatus(SupplierStatus.ACTIVE);
        }

        supplierRepository.save(supplier);
        return "Supplier status updated to " + supplier.getStatus().name();
    }

    @Override
    public SupplierResponseDto getSupplierById(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        return mapToResponseDto(supplier);
    }

    @Override
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponseDto> getActiveSuppliers() {
        return supplierRepository.findByStatus(SupplierStatus.ACTIVE)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private SupplierResponseDto mapToResponseDto(Supplier supplier) {
        return SupplierResponseDto.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .gstin(supplier.getGstin())
                .contactPerson(supplier.getContactPerson())
                .mobile(supplier.getMobile())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .bankDetails(supplier.getBankDetails())
                .status(supplier.getStatus())
                .totalPurchasedAmount(supplier.getTotalPurchasedAmount())
                .totalPaidAmount(supplier.getTotalPaidAmount())
                .outstandingBalance(supplier.getOutstandingBalance())
                .build();
    }
}