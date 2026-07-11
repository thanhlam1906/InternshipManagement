package com.example.internshipmanagement.config;

import com.example.internshipmanagement.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiter using a sliding window approach per user.
 * Tracks request counts per user per endpoint category within a configurable time window.
 *
 * Thread-safe: uses synchronized LRU LinkedHashMap + AtomicInteger.
 * Note: This is a single-instance solution. For multi-instance deployments, use Redis-based rate limiting.
 */
@Slf4j
@Component
public class RateLimiter {

    private final String activeProfile;

    /**
     * LRU cache of rate-limit windows, keyed by "userId:category".
     * Access-order eviction (max 10,000 entries) prevents unbounded growth.
     * Wrapped via {@link Collections#synchronizedMap} for thread safety.
     */
    private final Map<String, RateWindow> rateLimits = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, RateWindow> eldest) {
                    return size() > 10_000;
                }
            });

    public RateLimiter(@org.springframework.beans.factory.annotation.Value("${spring.profiles.active:}") String activeProfile) {
        this.activeProfile = activeProfile;
    }

    // Rate limit configurations
    private static final int CV_REVIEW_MAX_REQUESTS = 5;
    private static final long CV_REVIEW_WINDOW_MS = 60 * 60 * 1000L; // 1 hour

    private static final int JOB_SEARCH_MAX_REQUESTS = 30;
    private static final long JOB_SEARCH_WINDOW_MS = 60 * 60 * 1000L; // 1 hour

    /**
     * Check and consume a rate limit token for CV review.
     *
     * @param userId the user's ID
     * @throws RateLimitExceededException if the user has exceeded their limit
     */
    public void checkCVReviewLimit(Integer userId) {
        checkLimit(userId, "cv-review", CV_REVIEW_MAX_REQUESTS, CV_REVIEW_WINDOW_MS);
    }

    /**
     * Check and consume a rate limit token for job search.
     *
     * @param userId the user's ID
     * @throws RateLimitExceededException if the user has exceeded their limit
     */
    public void checkJobSearchLimit(Integer userId) {
        checkLimit(userId, "job-search", JOB_SEARCH_MAX_REQUESTS, JOB_SEARCH_WINDOW_MS);
    }

    private void checkLimit(Integer userId, String category, int maxRequests, long windowMs) {
        if ("dev".equals(activeProfile)) {
            log.info("Bypassing rate limit check in dev profile for userId={}, category={}", userId, category);
            return;
        }

        String key = userId + ":" + category;
        long now = System.currentTimeMillis();

        // Check current count BEFORE entering compute — avoids throwing inside the lambda.
        // (Minor TOCTOU race is acceptable here: at worst one extra request slips through.)
        RateWindow current = rateLimits.get(key);
        if (current != null && now - current.windowStart <= windowMs) {
            int currentCount = current.counter.get();
            if (currentCount >= maxRequests) {
                long remainingMs = windowMs - (now - current.windowStart);
                long remainingMinutes = Math.max(remainingMs / 60_000, 1);
                log.warn("Rate limit exceeded: userId={}, category={}, count={}/{}",
                        userId, category, currentCount, maxRequests);
                throw new RateLimitExceededException(
                        String.format("Ban da vuot qua gioi han %d request/%s. Vui long thu lai sau %d phut.",
                                maxRequests, category.equals("cv-review") ? "gio cho CV review" : "gio cho tim kiem job",
                                remainingMinutes));
            }
        }

        // Within limits (or new/expired window) — atomically increment or create.
        // Synchronized because Collections.synchronizedMap does not make compute() atomic.
        synchronized (rateLimits) {
            rateLimits.compute(key, (k, existing) -> {
                long nowInner = System.currentTimeMillis();
                if (existing == null || nowInner - existing.windowStart > windowMs) {
                    return new RateWindow(nowInner, new AtomicInteger(1));
                }
                existing.counter.incrementAndGet();
                return existing;
            });
        }
    }

    /**
     * Internal record to track a rate window's start time and counter.
     */
    private static class RateWindow {
        final long windowStart;
        final AtomicInteger counter;

        RateWindow(long windowStart, AtomicInteger counter) {
            this.windowStart = windowStart;
            this.counter = counter;
        }
    }
}
