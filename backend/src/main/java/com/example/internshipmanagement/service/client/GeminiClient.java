package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.config.ExternalApiProperties;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient extends AbstractAiReviewClient {

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper;

    // Timeout settings
    private static final int CONNECT_TIMEOUT_MS = 5_000;    // 5 seconds
    private static final int READ_TIMEOUT_MS = 30_000;      // 30 seconds

    public GeminiClient(ExternalApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = buildRestClient(
                properties.getGemini().getBaseUrl(), CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }

    @Override
    protected String providerName() {
        return "Gemini";
    }

    @Override
    protected void logApiFailure(String truncatedMessage, Exception e) {
        log.error("Gemini API call failed: {}", truncatedMessage, e);
    }

    @Override
    protected CVReviewResponse doReviewCV(String apiKey, String cvText, String major) {
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
            throw toExternalApiException(e);
        }
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

            return toCVReviewResponse(reviewJson);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly ket qua tu Gemini AI: " + e.getMessage(), e);
        }
    }
}
