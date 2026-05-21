package com.example.billing_backend.repository;

import com.example.billing_backend.model.RefreshToken;
import com.example.billing_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}