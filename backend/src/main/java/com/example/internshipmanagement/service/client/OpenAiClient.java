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
public class OpenAiClient {

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper;

    // Limit concurrent OpenAI API calls
    private static final int MAX_CONCURRENT_REQUESTS = 3;
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    // Timeout settings
    private static final int CONNECT_TIMEOUT_MS = 10_000;  // 10 seconds
    private static final int READ_TIMEOUT_MS    = 60_000;  // 60 seconds

    public OpenAiClient(ExternalApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(CONNECT_TIMEOUT_MS))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(java.time.Duration.ofMillis(READ_TIMEOUT_MS));

        this.restClient = RestClient.builder()
                .baseUrl(properties.getGroq().getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Send CV text to OpenAI Chat Completions API for review.
     * Applies concurrency control (max 3 simultaneous calls) and request timeouts.
     *
     * @param apiKey  the student's personal OpenAI API key (sk-...)
     * @param cvText  the extracted text from the CV
     * @param major   the student's major for relevance analysis
     * @return structured CV review response
     */
    public CVReviewResponse reviewCV(String apiKey, String cvText, String major) {
        validateApiKey(apiKey, "OpenAI");

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
        String model = properties.getGroq().getModel();
        String prompt = buildPrompt(cvText, major);

        // Build OpenAI Chat Completions request body
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.3,
                "max_tokens", 1500
        );

        try {
            log.info("Calling OpenAI API with model: {}", model);
            String responseBody = restClient.post()
                    .uri("/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseOpenAiResponse(responseBody);

        } catch (RestClientException e) {
            String errorDetail = e.getMessage() != null ? e.getMessage() : "Unknown error";
            String truncatedMsg = errorDetail.length() > 200 ? errorDetail.substring(0, 200) + "..." : errorDetail;
            log.error("OpenAI API call failed: {}", truncatedMsg, e);
            throw new ExternalApiException("Loi khi goi OpenAI API. Vui long kiem tra API key va thu lai.", e);
        }
    }

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
                    "contentFeedback": "<nhan xet chi ve noi dung, kinh nghiem, ky nang phan tich cac du an xem da viet hoan chinh chua>",
                    "strengthPoints": "<cac diem manh cua CV>",
                    "improvementSuggestions": "<goi y cai thien cu the theo tung du an trong cv da danh gia>",
                    
                }
                """.formatted(major, cvText, major);
    }

    private CVReviewResponse parseOpenAiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // OpenAI response structure: choices[0].message.content
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new ExternalApiException("OpenAI API khong tra ve ket qua. Vui long thu lai.");
            }

            String generatedText = choices.get(0)
                    .path("message")
                    .path("content")
                    .asText();

            // Strip markdown code fences if present (e.g. ```json ... ```)
            String jsonText = generatedText.trim();
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("^```(?:json)?\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            JsonNode reviewJson = objectMapper.readTree(jsonText);

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
            log.error("Failed to parse OpenAI response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly ket qua tu OpenAI: " + e.getMessage(), e);
        }
    }
}
