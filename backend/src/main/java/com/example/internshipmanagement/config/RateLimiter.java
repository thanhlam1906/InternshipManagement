package com.example.internshipmanagement.config;

import com.example.internshipmanagement.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiter using a sliding window approach per user.
 * Tracks request counts per user per endpoint category within a configurable time window.
 *
 * Thread-safe: uses ConcurrentHashMap + AtomicInteger.
 * Note: This is a single-instance solution. For multi-instance deployments, use Redis-based rate limiting.
 */
@Slf4j
@Component
public class RateLimiter {

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:}")
    private String activeProfile;

    // Key: "userId:category", Value: window tracker
    private final Map<String, RateWindow> rateLimits = new ConcurrentHashMap<>();

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

        RateWindow window = rateLimits.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();

            if (existing == null || now - existing.windowStart > windowMs) {
                // New window or expired window — reset
                return new RateWindow(now, new AtomicInteger(1));
            }

            // Within window — increment
            int currentCount = existing.counter.incrementAndGet();
            if (currentCount > maxRequests) {
                long remainingMs = windowMs - (now - existing.windowStart);
                long remainingMinutes = remainingMs / 60_000;
                log.warn("Rate limit exceeded: userId={}, category={}, count={}/{}",
                        userId, category, currentCount, maxRequests);
                throw new RateLimitExceededException(
                        String.format("Ban da vuot qua gioi han %d request/%s. Vui long thu lai sau %d phut.",
                                maxRequests, category.equals("cv-review") ? "gio cho CV review" : "gio cho tim kiem job",
                                remainingMinutes));
            }

            return existing;
        });
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
