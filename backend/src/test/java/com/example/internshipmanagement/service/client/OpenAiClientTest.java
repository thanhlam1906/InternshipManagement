package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.config.ExternalApiProperties;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class OpenAiClientTest {

    private static final String VALID_API_KEY = "groq-api-key-1234567890";

    private static final String REVIEW_JSON = """
            {
                "overallScore": 9,
                "summary": "CV rat tot",
                "contentFeedback": "Trinh bay ro rang",
                "strengthPoints": "Ky nang da dang",
                "improvementSuggestions": "Bo sung chung chi"
            }
            """;

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OpenAiClient openAiClient;

    @BeforeEach
    void setUp() {
        ExternalApiProperties properties = new ExternalApiProperties();
        openAiClient = new OpenAiClient(properties, objectMapper);
        // Thay RestClient that (tao trong constructor) bang mock
        ReflectionTestUtils.setField(openAiClient, "restClient", restClient);
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

    private String openAiResponseWith(String content) {
        // Cau truc response cua OpenAI: choices[0].message.content
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode message = objectMapper.createObjectNode().put("content", content);
        ObjectNode choice = objectMapper.createObjectNode();
        choice.set("message", message);
        root.set("choices", objectMapper.createArrayNode().add(choice));
        return root.toString();
    }

    private void assertFullReview(CVReviewResponse response) {
        assertThat(response.getOverallScore()).isEqualTo(9);
        assertThat(response.getSummary()).isEqualTo("CV rat tot");
        assertThat(response.getContentFeedback()).isEqualTo("Trinh bay ro rang");
        assertThat(response.getStrengthPoints()).isEqualTo("Ky nang da dang");
        assertThat(response.getImprovementSuggestions()).isEqualTo("Bo sung chung chi");
    }

    // ==================== API key validation ====================

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsNull() {
        assertThatThrownBy(() -> openAiClient.reviewCV(null, "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenAI API key khong duoc de trong");

        verifyNoInteractions(restClient);
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsBlank() {
        assertThatThrownBy(() -> openAiClient.reviewCV("   ", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenAI API key khong duoc de trong");

        verifyNoInteractions(restClient);
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyTooShort() {
        assertThatThrownBy(() -> openAiClient.reviewCV("gsk_1", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenAI API key khong hop le (qua ngan)");

        verifyNoInteractions(restClient);
    }

    // ==================== parse response ====================

    @Test
    void reviewCV_ShouldReturnCVReviewResponse_WhenContentIsPlainJson() {
        stubApiResponse(openAiResponseWith(REVIEW_JSON));

        CVReviewResponse response = openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT");

        assertFullReview(response);
        assertThat(response.getReviewedAt()).isNotNull();
    }

    @Test
    void reviewCV_ShouldStripMarkdownFence_WhenContentWrappedInJsonCodeBlock() {
        String fenced = "```json\n" + REVIEW_JSON + "\n```";
        stubApiResponse(openAiResponseWith(fenced));

        CVReviewResponse response = openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT");

        assertFullReview(response);
    }

    @Test
    void reviewCV_ShouldStripMarkdownFence_WhenContentWrappedInPlainCodeBlock() {
        // Fence khong co tag "json"
        String fenced = "```\n" + REVIEW_JSON + "\n```";
        stubApiResponse(openAiResponseWith(fenced));

        CVReviewResponse response = openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT");

        assertFullReview(response);
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenChoicesIsEmpty() {
        stubApiResponse("{\"choices\":[]}");

        assertThatThrownBy(() -> openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("OpenAI API khong tra ve ket qua");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenChoicesIsMissing() {
        stubApiResponse("{}");

        assertThatThrownBy(() -> openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("OpenAI API khong tra ve ket qua");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenContentIsNotJson() {
        stubApiResponse(openAiResponseWith("xin loi, toi khong the danh gia CV nay"));

        assertThatThrownBy(() -> openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Loi khi xu ly ket qua tu OpenAI");
    }

    @Test
    void reviewCV_ShouldThrowExternalApiException_WhenRestClientFails() {
        stubRequestChain();
        when(responseSpec.body(String.class))
                .thenThrow(new RestClientException("429 Too Many Requests"));

        assertThatThrownBy(() -> openAiClient.reviewCV(VALID_API_KEY, "cv text", "IT"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Loi khi goi OpenAI API")
                .hasCauseInstanceOf(RestClientException.class);
    }
}
