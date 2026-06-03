package com.example.billing_backend.service;

import com.example.billing_backend.dto.CustomerRequestDto;
import com.example.billing_backend.dto.CustomerResponseDto;
import com.example.billing_backend.model.Customer;
import com.example.billing_backend.model.CustomerStatus;
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

        // AUTOMATION RULE 1: Strict Blacklist Status Interception
        if (customer.getStatus() == CustomerStatus.BLACKLISTED) {
            throw new IllegalArgumentException("Transaction blocked: Customer account is permanently BLACKLISTED due to unpaid long-term arrears!");
        }

        double currentDebt = customer.getOutstandingDebt() != null ? customer.getOutstandingDebt() : 0.0;
        double bill = billAmount != null ? billAmount : 0.0;
        double paid = paidAmount != null ? paidAmount : 0.0;

        // AUTOMATION RULE 2: Arrears Overdue Aging Check (30 Days Limit)
        if (currentDebt > 0 && customer.getLastCreditDateTime() != null) {
            long daysOverdue = java.time.Duration.between(customer.getLastCreditDateTime(), java.time.LocalDateTime.now()).toDays();
            if (daysOverdue >= 30) {
                customer.setStatus(CustomerStatus.BLACKLISTED);
                customerRepository.save(customer); // Immediate status persistence lock
                throw new IllegalArgumentException("Transaction blocked: Overdue calculation audit failed! Account blacklisted due to unpaid arrears for " + daysOverdue + " days.");
            }
        }

        double newDebt = (currentDebt + bill) - paid;

        // Credit Limit Firewall Validation
        if (newDebt > customer.getCreditLimit()) {
            throw new IllegalArgumentException("Transaction blocked: Outstanding debt exceeds customer's credit limit!");
        }

        // Apply ledger data adjustments if validation succeeds
        if (bill > 0) {
            customer.setTotalSpentAmount(customer.getTotalSpentAmount() + bill);
        }

        customer.setOutstandingDebt(newDebt < 0 ? 0 : newDebt);

        // AUTOMATION RULE 3: Smart Tracking Timestamp Life-cycle Management
        if (customer.getOutstandingDebt() > 0) {
            if (customer.getLastCreditDateTime() == null) {
                customer.setLastCreditDateTime(java.time.LocalDateTime.now());
            }
        } else {
            customer.setLastCreditDateTime(null); // Debt cleared reset loop
        }

        // Tier Promotion Logic
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
                .status(customer.getStatus())
                .lastCreditDateTime(customer.getLastCreditDateTime())
                .build();
    }
    @Override
    public CustomerResponseDto changeCustomerStatus(Integer id, com.example.billing_backend.model.CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setStatus(status);
        if (status == CustomerStatus.ACTIVE && customer.getOutstandingDebt() > 0) {
            customer.setLastCreditDateTime(java.time.LocalDateTime.now());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponseDto(updatedCustomer);
    }
    @Override
    public void deleteCustomer(Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found!");
        }
        customerRepository.deleteById(id);
    }


}