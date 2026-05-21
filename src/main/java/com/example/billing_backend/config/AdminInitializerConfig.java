package com.example.billing_backend.config;

import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.model.UserStatus;
import com.example.billing_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializerConfig {

    @Bean
    CommandLineRunner initAdminAccount(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultAdminEmail = "admin@gmail.com";

            if (userRepository.findByEmail(defaultAdminEmail).isEmpty()) {
                User superAdmin = User.builder()
                        .name("Super Admin")
                        .email(defaultAdminEmail)
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(superAdmin);

                System.out.println("==================================================");
                System.out.println("NEXBILL ERP AUTOMATION: Super Admin account seeded successfully!");
                System.out.println("Default Username: admin@gmail.com");
                System.out.println("Default Password: Admin@123");
                System.out.println("==================================================");
            }
        };
    }
}