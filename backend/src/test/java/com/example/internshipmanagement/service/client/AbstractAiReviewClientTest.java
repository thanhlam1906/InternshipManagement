package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Unit test cho template method {@link AbstractAiReviewClient}:
 * validate API key, semaphore concurrency, buildPrompt, toCVReviewResponse,
 * toExternalApiException.
 */
class AbstractAiReviewClientTest {

    private static final String VALID_API_KEY = "valid-api-key-1234567890";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Concrete subclass dung cho test — khong goi HTTP that. */
    private static class TestAiClient extends AbstractAiReviewClient {

        private final AtomicInteger currentConcurrent = new AtomicInteger(0);
        private final AtomicInteger maxConcurrent = new AtomicInteger(0);
        private RuntimeException toThrow;
        private long sleepMs = 0;

        @Override
        protected String providerName() {
            return "TestProvider";
        }

        @Override
        protected CVReviewResponse doReviewCV(String apiKey, String cvText, String major) {
            int current = currentConcurrent.incrementAndGet();
            maxConcurrent.accumulateAndGet(current, Math::max);
            try {
                if (sleepMs > 0) {
                    Thread.sleep(sleepMs);
                }
                if (toThrow != null) {
                    throw toThrow;
                }
                return CVReviewResponse.builder()
                        .overallScore(8)
                        .summary("cv=" + cvText + ";major=" + major)
                        .build();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                currentConcurrent.decrementAndGet();
            }
        }

        @Override
        protected void logApiFailure(String truncatedMessage, Exception e) {
            // no-op trong test
        }

        // Expose protected members cho test
        String promptOf(String cvText, String major) {
            return buildPrompt(cvText, major);
        }

        CVReviewResponse mapResponse(JsonNode node) {
            return toCVReviewResponse(node);
        }

        ExternalApiException wrap(RestClientException e) {
            return toExternalApiException(e);
        }
    }

    // ==================== validateApiKey ====================

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsNull() {
        TestAiClient client = new TestAiClient();

        assertThatThrownBy(() -> client.reviewCV(null, "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TestProvider")
                .hasMessageContaining("khong duoc de trong");
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyIsBlank() {
        TestAiClient client = new TestAiClient();

        assertThatThrownBy(() -> client.reviewCV("   ", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("khong duoc de trong");
    }

    @Test
    void reviewCV_ShouldThrowIllegalArgumentException_WhenApiKeyTooShort() {
        TestAiClient client = new TestAiClient();

        assertThatThrownBy(() -> client.reviewCV("short", "cv text", "IT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("qua ngan");
    }

    // ==================== delegate ====================

    @Test
    void reviewCV_ShouldReturnResponseFromDoReviewCV_WhenApiKeyValid() {
        TestAiClient client = new TestAiClient();

        CVReviewResponse response = client.reviewCV(VALID_API_KEY, "my cv", "CNTT");

        assertThat(response).isNotNull();
        assertThat(response.getOverallScore()).isEqualTo(8);
        assertThat(response.getSummary()).isEqualTo("cv=my cv;major=CNTT");
    }

    // ==================== semaphore ====================

    @Test
    void reviewCV_ShouldReleaseSemaphore_WhenDoReviewCVThrows() {
        TestAiClient client = new TestAiClient();
        client.toThrow = new IllegalStateException("boom");

        // Goi tuan tu nhieu hon so permit (3). Neu semaphore bi leak,
        // lan goi thu 4 se block 10s roi throw ExternalApiException.
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (int i = 0; i < 5; i++) {
                assertThatThrownBy(() -> client.reviewCV(VALID_API_KEY, "cv", "IT"))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("boom");
            }
        });
    }

    @Test
    void reviewCV_ShouldLimitConcurrentCallsToThree_WhenManyParallelRequests() throws Exception {
        TestAiClient client = new TestAiClient();
        client.sleepMs = 200;

        int threads = 6;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<CVReviewResponse>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();
                    return client.reviewCV(VALID_API_KEY, "cv", "IT");
                }));
            }
            startLatch.countDown();
            for (Future<CVReviewResponse> future : futures) {
                assertThat(future.get(15, TimeUnit.SECONDS)).isNotNull();
            }
        } finally {
            executor.shutdownNow();
        }

        // Toi da 3 request chay dong thoi
        assertThat(client.maxConcurrent.get()).isLessThanOrEqualTo(3);
        assertThat(client.maxConcurrent.get()).isGreaterThan(0);
    }

    // ==================== buildPrompt ====================

    @Test
    void buildPrompt_ShouldContainCvTextAndMajor_WhenCalled() {
        TestAiClient client = new TestAiClient();

        String prompt = client.promptOf("Java developer voi 2 du an", "Cong nghe thong tin");

        assertThat(prompt)
                .contains("Java developer voi 2 du an")
                .contains("Cong nghe thong tin")
                .contains("overallScore")
                .contains("summary")
                .contains("contentFeedback")
                .contains("strengthPoints")
                .contains("improvementSuggestions");
    }

    // ==================== toCVReviewResponse ====================

    @Test
    void toCVReviewResponse_ShouldMapAllFields_WhenJsonComplete() throws Exception {
        TestAiClient client = new TestAiClient();
        JsonNode node = objectMapper.readTree("""
                {
                    "overallScore": 7,
                    "summary": "CV tot",
                    "contentFeedback": "Noi dung ro rang",
                    "strengthPoints": "Kinh nghiem thuc te",
                    "improvementSuggestions": "Bo sung so lieu"
                }
                """);

        CVReviewResponse response = client.mapResponse(node);

        assertThat(response.getOverallScore()).isEqualTo(7);
        assertThat(response.getSummary()).isEqualTo("CV tot");
        assertThat(response.getContentFeedback()).isEqualTo("Noi dung ro rang");
        assertThat(response.getStrengthPoints()).isEqualTo("Kinh nghiem thuc te");
        assertThat(response.getImprovementSuggestions()).isEqualTo("Bo sung so lieu");
        assertThat(response.getReviewedAt()).isNotNull();
    }

    @Test
    void toCVReviewResponse_ShouldUseDefaults_WhenFieldsMissing() throws Exception {
        TestAiClient client = new TestAiClient();
        JsonNode node = objectMapper.readTree("{}");

        CVReviewResponse response = client.mapResponse(node);

        assertThat(response.getOverallScore()).isZero();
        assertThat(response.getSummary()).isEmpty();
        assertThat(response.getContentFeedback()).isEmpty();
        assertThat(response.getStrengthPoints()).isEmpty();
        assertThat(response.getImprovementSuggestions()).isEmpty();
    }

    // ==================== toExternalApiException ====================

    @Test
    void toExternalApiException_ShouldWrapWithProviderMessage_WhenRestClientFails() {
        TestAiClient client = new TestAiClient();
        RestClientException cause = new RestClientException("connection refused");

        ExternalApiException wrapped = client.wrap(cause);

        assertThat(wrapped.getMessage()).contains("TestProvider");
        assertThat(wrapped.getMessage()).contains("API key");
        assertThat(wrapped.getCause()).isSameAs(cause);
    }

    @Test
    void toExternalApiException_ShouldNotFail_WhenCauseMessageIsVeryLong() {
        TestAiClient client = new TestAiClient();
        RestClientException cause = new RestClientException("x".repeat(500));

        ExternalApiException wrapped = client.wrap(cause);

        assertThat(wrapped.getMessage()).contains("TestProvider");
        assertThat(wrapped.getCause()).isSameAs(cause);
    }
}
