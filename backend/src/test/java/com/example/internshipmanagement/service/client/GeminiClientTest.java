package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.config.ExternalApiProperties;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeminiClientTest {

    private static final String VALID_API_KEY = "gemini-api-key-1234567890";

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() {
        ExternalApiProperties properties = new ExternalApiProperties();
        geminiClient = new GeminiClient(properties, objectMapper);
        // Thay RestClient that (tao trong constructor) bang mock
        ReflectionTestUtils.setField(geminiClient, "restClient", restClient);
    }

    private void stubRequestChain() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    private void stubApiResponse(String responseBody) {
        stubRequestChain();
        when(responseSpec.body(String.class)).thenReturn(responseBody);
    }

    private String geminiResponseWith(String generatedText) {
        // Cau truc response cua Gemini: candidates[0].content.parts[0].text
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode().put("text", generatedText);
        ObjectNode content = objectMapper.createObjectNode();
        content.set("parts", objectMapper.createArrayNode().add(part));
        ObjectNode candidate = objectMapper.createObjectNode();
        candidate.set("content", content);
        root.set("candidates", objectMapper.createArrayNode().add(candidate));
        return root.toString();
    }

    // ==================== API key validation ====================

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsNull() {
        assertThatThrownBy(() -> geminiClient.reviewCV(null, "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gemini API key khong duoc de trong");

        verifyNoInteractions(restClient);
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsBlank() {
        assertThatThrownBy(() -> geminiClient.reviewCV("  ", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gemini API key khong duoc de trong");

        verifyNoInteractions(restClient);
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyTooShort() {
        assertThatThrownBy(() -> geminiClient.reviewCV("abc123", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gemini API key khong hop le (qua ngan)");

        verifyNoInteractions(restClient);
    }

    // ==================== parse response ====================

    @Test
    void reviewCV_ShouldReturnCVReviewResponse_WhenApiReturnsValidResponse() {
        String reviewJson = """
                {
                    "overallScore": 8,
                    "summary": "CV chat luong tot",
                    "contentFeedback": "Noi dung day du",
                    "strengthPoints": "Nhieu du an thuc te",
                    "improvementSuggestions": "Them so lieu cu the"
                }
                """;
        stubApiResponse(geminiResponseWith(reviewJson));

        CVReviewResponse response = geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT");

        assertThat(response.getOverallScore()).isEqualTo(8);
        assertThat(response.getSummary()).isEqualTo("CV chat luong tot");
        assertThat(response.getContentFeedback()).isEqualTo("Noi dung day du");
        assertThat(response.getStrengthPoints()).isEqualTo("Nhieu du an thuc te");
        assertThat(response.getImprovementSuggestions()).isEqualTo("Them so lieu cu the");
        assertThat(response.getReviewedAt()).isNotNull();
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenCandidatesIsEmpty() {
        stubApiResponse("{\"candidates\":[]}");

        assertThatThrownBy(() -> geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Gemini API khong tra ve ket qua");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenCandidatesIsMissing() {
        stubApiResponse("{}");

        assertThatThrownBy(() -> geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Gemini API khong tra ve ket qua");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenGeneratedTextIsNotJson() {
        // Gemini parse truc tiep — text khong phai JSON hop le se fail
        stubApiResponse(geminiResponseWith("day khong phai JSON"));

        assertThatThrownBy(() -> geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Loi khi xu ly ket qua tu Gemini AI");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenResponsePartsMissing() {
        // candidates ton tai nhung thieu content.parts → loi khi parse
        stubApiResponse("{\"candidates\":[{\"content\":{}}]}");

        assertThatThrownBy(() -> geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Loi khi xu ly ket qua tu Gemini AI");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenRestClientFails() {
        stubRequestChain();
        when(responseSpec.body(String.class))
                .thenThrow(new RestClientException("401 Unauthorized"));

        assertThatThrownBy(() -> geminiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Loi khi goi Gemini API")
                .hasCauseInstanceOf(RestClientException.class);
    }
}
