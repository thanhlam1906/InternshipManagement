package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Base class for AI CV review clients (Gemini, OpenAI, ...).
 * Centralizes the shared logic: API key validation, concurrency control
 * (semaphore), prompt building, HTTP error handling and mapping the parsed
 * review JSON into {@link CVReviewResponse}.
 * Subclasses only implement the provider-specific request/response handling.
 */
public abstract class AbstractAiReviewClient {

    // Limit concurrent AI API calls to prevent thread pool exhaustion
    private static final int MAX_CONCURRENT_REQUESTS = 3;
    private static final int SEMAPHORE_WAIT_SECONDS = 10;

    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    /** Provider display name used in logs and error messages (e.g. "Gemini"). */
    protected abstract String providerName();

    /** Provider-specific API call + response parsing. */
    protected abstract CVReviewResponse doReviewCV(String apiKey, String cvText, String major);

    /**
     * Send CV text to the AI provider for review.
     * Applies concurrency control (max 3 simultaneous calls).
     *
     * @param apiKey  the student's personal API key
     * @param cvText  the extracted text from the CV
     * @param major   the student's major for relevance analysis
     * @return structured CV review response
     */
    public CVReviewResponse reviewCV(String apiKey, String cvText, String major) {
        validateApiKey(apiKey);

        boolean acquired;
        try {
            acquired = semaphore.tryAcquire(SEMAPHORE_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Yeu cau bi gian doan. Vui long thu lai.");
        }

        if (!acquired) {
            throw new ExternalApiException(
                    "He thong dang xu ly qua nhieu yeu cau. Vui long thu lai sau vai giay.");
        }

        try {
            return doReviewCV(apiKey, cvText, major);
        } finally {
            semaphore.release();
        }
    }

    /**
     * Validate API key has a reasonable format before making an external call.
     */
    private void validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(providerName() + " API key khong duoc de trong");
        }
        if (apiKey.length() < 10) {
            throw new IllegalArgumentException(providerName() + " API key khong hop le (qua ngan)");
        }
    }

    protected String buildPrompt(String cvText, String major) {
        return """
                Ban la mot chuyen gia tuyen dung va review CV voi 10 nam kinh nghiem.
                Hay danh gia CV duoi day cua mot sinh vien nganh "%s" dang tim kiem co hoi thuc tap.
                
                CV content:
                ---
                %s
                ---
                
                Hay tra ve danh gia theo dinh dang JSON chinh xac nhu sau (khong them bat ky text nao khac):
                {
                    "overallScore": <diem tu 1 den 10>,
                    "summary": "<tom tat ngan gon ve CV>",
                    "contentFeedback": "<nhan xet chi ve noi dung, kinh nghiem, ky nang phan tich cac du an xem da viet hoan chinh chua>",
                    "strengthPoints": "<cac diem manh cua CV>",
                    "improvementSuggestions": "<goi y cai thien cu the theo tung du an trong cv da danh gia>",
                    
                }
                """.formatted(major, cvText, major);
    }

    /** Map the parsed review JSON node into a {@link CVReviewResponse}. */
    protected CVReviewResponse toCVReviewResponse(JsonNode reviewJson) {
        return CVReviewResponse.builder()
                .overallScore(reviewJson.path("overallScore").asInt(0))
                .summary(reviewJson.path("summary").asText(""))
                .contentFeedback(reviewJson.path("contentFeedback").asText(""))
                .strengthPoints(reviewJson.path("strengthPoints").asText(""))
                .improvementSuggestions(reviewJson.path("improvementSuggestions").asText(""))
                .reviewedAt(LocalDateTime.now())
                .build();
    }

    /** Build a RestClient with connect/read timeouts configured. */
    protected static RestClient buildRestClient(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(java.time.Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /** Wrap a RestClientException into ExternalApiException with truncated logging. */
    protected ExternalApiException toExternalApiException(RestClientException e) {
        String errorDetail = e.getMessage() != null ? e.getMessage() : "Unknown error";
        // Only log first 200 chars of error to avoid leaking sensitive data
        String truncatedMsg = errorDetail.length() > 200 ? errorDetail.substring(0, 200) + "..." : errorDetail;
        logApiFailure(truncatedMsg, e);
        return new ExternalApiException(
                "Loi khi goi " + providerName() + " API. Vui long kiem tra API key va thu lai.", e);
    }

    /** Subclasses log with their own logger so log source stays accurate. */
    protected abstract void logApiFailure(String truncatedMessage, Exception e);
}
