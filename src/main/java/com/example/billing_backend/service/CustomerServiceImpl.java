package com.example.billing_backend.service;

import com.example.billing_backend.dto.CustomerRequestDto;
import com.example.billing_backend.dto.CustomerResponseDto;
import com.example.billing_backend.model.Customer;
import com.example.billing_backend.model.CustomerTier;
import com.example.billing_backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponseDto addCustomer(CustomerRequestDto request) {
        if (customerRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new RuntimeException("Customer with this mobile number already exists!");
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : 0.0)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponseDto(savedCustomer);
    }

    @Override
    public CustomerResponseDto getCustomerByMobile(String mobile) {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Customer not found for this mobile number!"));
        return mapToResponseDto(customer);
    }

    @Override
    public CustomerResponseDto updateCustomer(Integer id, CustomerRequestDto request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(request.getName());
        customer.setMobile(request.getMobile());
        customer.setEmail(request.getEmail());
        if(request.getCreditLimit() != null) {
            customer.setCreditLimit(request.getCreditLimit());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponseDto(updatedCustomer);
    }

    @Override
    public CustomerResponseDto updateCustomerLedger(Integer id, Double billAmount, Double paidAmount) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (billAmount != null && billAmount > 0) {
            customer.setTotalSpentAmount(customer.getTotalSpentAmount() + billAmount);
        }

        double newDebt = (customer.getOutstandingDebt() + (billAmount != null ? billAmount : 0)) - (paidAmount != null ? paidAmount : 0);

        if (newDebt > customer.getCreditLimit()) {
            // Hard crash pannaama cashier readable structure message standard frame return response pass matrix return loop
            return CustomerResponseDto.builder()
                    .id(customer.getId())
                    .name(customer.getName())
                    .mobile(customer.getMobile())
                    .email(customer.getEmail())
                    .tier(customer.getTier())
                    .totalSpentAmount(customer.getTotalSpentAmount())
                    .creditLimit(customer.getCreditLimit())
                    .outstandingDebt(customer.getOutstandingDebt())
                    // Ithu kulla oru error/status key code tracking string custom property unga AuthResponse update patterns template match-la return pannanum nane.
                    .build();
        }

        customer.setOutstandingDebt(newDebt < 0 ? 0 : newDebt);

        if (customer.getTotalSpentAmount() >= 100000) {
            customer.setTier(CustomerTier.CORPORATE);
        } else if (customer.getTotalSpentAmount() >= 20000) {
            customer.setTier(CustomerTier.VIP);
        }

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponseDto(savedCustomer);
    }

    @Override
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private CustomerResponseDto mapToResponseDto(Customer customer) {
        return CustomerResponseDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .mobile(customer.getMobile())
                .email(customer.getEmail())
                .tier(customer.getTier())
                .totalSpentAmount(customer.getTotalSpentAmount())
                .creditLimit(customer.getCreditLimit())
                .outstandingDebt(customer.getOutstandingDebt())
                .build();
    }
}