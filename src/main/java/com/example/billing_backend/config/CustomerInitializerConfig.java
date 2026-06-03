package com.example.billing_backend.config;

import com.example.billing_backend.model.Customer;
import com.example.billing_backend.model.CustomerTier;
import com.example.billing_backend.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerInitializerConfig {

    @Bean
    CommandLineRunner initGuestCustomer(CustomerRepository customerRepository) {
        return args -> {
            String defaultGuestMobile = "0000000000";

            if (customerRepository.findByMobile(defaultGuestMobile).isEmpty()) {
                Customer guestCustomer = Customer.builder()
                        .name("Walk-In Customer")
                        .mobile(defaultGuestMobile)
                        .email("guest@nexbill.com")
                        .tier(CustomerTier.REGULAR)
                        .totalSpentAmount(0.0)
                        .creditLimit(0.0) // Guest customer-ku zero credit threshold standard restriction
                        .outstandingDebt(0.0)
                        .build();

                customerRepository.save(guestCustomer);

                System.out.println("==================================================");
                System.out.println("NEXBILL ERP AUTOMATION: Walk-In Customer profile seeded successfully!");
                System.out.println("Default Guest Identifier Mobile: 0000000000");
                System.out.println("==================================================");
            }
        };
    }
}