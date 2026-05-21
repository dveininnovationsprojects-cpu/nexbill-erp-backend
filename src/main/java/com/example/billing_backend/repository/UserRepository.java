package com.example.billing_backend.repository;

import com.example.billing_backend.model.Role;
import com.example.billing_backend.model.User;
import com.example.billing_backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatus(Role role, UserStatus status);
}