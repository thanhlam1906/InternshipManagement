package com.example.internshipmanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory JWT token blacklist for logout functionality.
 * Tokens added here are invalidated before their natural expiry.
 * Entries are auto-cleaned every 60 seconds.
 */
@Slf4j
@Component
public class TokenBlacklist {

    // Map<tokenHash, expiryTimestamp>
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String tokenHash, Instant expiry) {
        blacklistedTokens.put(tokenHash, expiry.toEpochMilli());
        log.debug("Token blacklisted until {}", expiry);
    }

    public boolean isBlacklisted(String tokenHash) {
        return blacklistedTokens.containsKey(tokenHash);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
