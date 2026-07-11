package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.config.ExternalApiProperties;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper;

    // Limit concurrent Gemini API calls to prevent thread pool exhaustion
    private static final int MAX_CONCURRENT_REQUESTS = 3;
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    // Timeout settings
    private static final int CONNECT_TIMEOUT_MS = 5_000;    // 5 seconds
    private static final int READ_TIMEOUT_MS = 30_000;      // 30 seconds

    public GeminiClient(ExternalApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        // Configure timeouts with pooled HTTP client
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(CONNECT_TIMEOUT_MS))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(java.time.Duration.ofMillis(READ_TIMEOUT_MS));

        this.restClient = RestClient.builder()
                .baseUrl(properties.getGemini().getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Send CV text to Gemini API for review and parse the structured response.
     * Applies concurrency control (max 3 simultaneous calls) and request timeouts.
     *
     * @param apiKey  the student's personal Gemini API key
     * @param cvText  the extracted text from the CV
     * @param major   the student's major for relevance analysis
     * @return structured CV review response
     */
    public CVReviewResponse reviewCV(String apiKey, String cvText, String major) {
        // Validate API key format
        validateApiKey(apiKey, "Gemini");

        // Acquire semaphore permit (concurrency control)
        boolean acquired;
        try {
            acquired = semaphore.tryAcquire(10, TimeUnit.SECONDS);
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

    private CVReviewResponse doReviewCV(String apiKey, String cvText, String major) {
        String model = properties.getGemini().getModel();
        String url = String.format("/models/%s:generateContent", model);

        String prompt = buildPrompt(cvText, major);

        // Build Gemini API request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            String responseBody = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(responseBody);

        } catch (RestClientException e) {
            String errorDetail = e.getMessage() != null ? e.getMessage() : "Unknown error";
            // Only log first 200 chars of error to avoid leaking sensitive data
            String truncatedMsg = errorDetail.length() > 200 ? errorDetail.substring(0, 200) + "..." : errorDetail;
            log.error("Gemini API call failed: {}", truncatedMsg, e);
            throw new ExternalApiException("Loi khi goi Gemini API. Vui long kiem tra API key va thu lai.", e);
        }
    }

    /**
     * Validate API key has a reasonable format before making an external call.
     */
    private void validateApiKey(String apiKey, String provider) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(provider + " API key khong duoc de trong");
        }
        if (apiKey.length() < 10) {
            throw new IllegalArgumentException(provider + " API key khong hop le (qua ngan)");
        }
    }

    private String buildPrompt(String cvText, String major) {
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
                    "formatFeedback": "<nhan xet ve dinh dang, bo cuc, trinh bay>",
                    "contentFeedback": "<nhan xet ve noi dung, kinh nghiem, ky nang>",
                    "strengthPoints": "<cac diem manh cua CV>",
                    "improvementSuggestions": "<goi y cai thien cu the>",
                    "majorRelevance": "<danh gia muc do phu hop voi nganh %s>"
                }
                """.formatted(major, cvText, major);
    }

    private CVReviewResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Gemini response structure: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new ExternalApiException("Gemini API khong tra ve ket qua. Vui long thu lai.");
            }

            String generatedText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // Parse the JSON from generated text
            JsonNode reviewJson = objectMapper.readTree(generatedText);

            return CVReviewResponse.builder()
                    .overallScore(reviewJson.path("overallScore").asInt(0))
                    .summary(reviewJson.path("summary").asText(""))
                    .formatFeedback(reviewJson.path("formatFeedback").asText(""))
                    .contentFeedback(reviewJson.path("contentFeedback").asText(""))
                    .strengthPoints(reviewJson.path("strengthPoints").asText(""))
                    .improvementSuggestions(reviewJson.path("improvementSuggestions").asText(""))
                    .majorRelevance(reviewJson.path("majorRelevance").asText(""))
                    .reviewedAt(LocalDateTime.now())
                    .build();

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly ket qua tu Gemini AI: " + e.getMessage(), e);
        }
    }
}
