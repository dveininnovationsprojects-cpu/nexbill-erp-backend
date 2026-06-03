package com.example.billing_backend.service;

import com.example.billing_backend.dto.CustomerRequestDto;
import com.example.billing_backend.dto.CustomerResponseDto;
import com.example.billing_backend.model.CustomerStatus;

import java.util.List;

public interface CustomerService {
    CustomerResponseDto addCustomer(CustomerRequestDto request);
    CustomerResponseDto getCustomerByMobile(String mobile);
    CustomerResponseDto updateCustomer(Integer id, CustomerRequestDto request);
    CustomerResponseDto updateCustomerLedger(Integer id, Double billAmount, Double paidAmount);
    List<CustomerResponseDto> getAllCustomers();
    CustomerResponseDto changeCustomerStatus(Integer id, CustomerStatus status);
    void deleteCustomer(Integer id);
}