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
public class OpenAiClient extends AbstractAiReviewClient {

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper;

    // Timeout settings
    private static final int CONNECT_TIMEOUT_MS = 10_000;  // 10 seconds
    private static final int READ_TIMEOUT_MS    = 60_000;  // 60 seconds

    public OpenAiClient(ExternalApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = buildRestClient(
                properties.getGroq().getBaseUrl(), CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }

    @Override
    protected String providerName() {
        return "OpenAI";
    }

    @Override
    protected void logApiFailure(String truncatedMessage, Exception e) {
        log.error("OpenAI API call failed: {}", truncatedMessage, e);
    }

    @Override
    protected CVReviewResponse doReviewCV(String apiKey, String cvText, String major) {
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
            throw toExternalApiException(e);
        }
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

            return toCVReviewResponse(reviewJson);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly ket qua tu OpenAI: " + e.getMessage(), e);
        }
    }
}
