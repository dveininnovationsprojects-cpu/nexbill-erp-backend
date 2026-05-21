package com.example.billing_backend.config;

import com.example.billing_backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void wipeExpiredTokensDaily() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        System.out.println("NEXBILL ERP AUTOMATION: Expired refresh tokens wiped out from database.");
    }
}