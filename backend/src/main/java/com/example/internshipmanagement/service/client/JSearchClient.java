package com.example.internshipmanagement.service.client;

import com.example.internshipmanagement.config.ExternalApiProperties;
import com.example.internshipmanagement.dto.response.job.ExternalJobResponse;
import com.example.internshipmanagement.dto.response.job.JobSearchResultResponse;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JSearchClient {

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper;

    // Timeout settings
    private static final int CONNECT_TIMEOUT_MS = 5_000;   // 5 seconds
    private static final int READ_TIMEOUT_MS = 30_000;     // 30 seconds

    public JSearchClient(ExternalApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        // Configure timeouts
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getJsearch().getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Search jobs on JSearch API using system-wide credentials.
     *
     * @param keyword        search keyword (e.g. student's major)
     * @param location       location filter (optional)
     * @param page           page number (1-based)
     * @param resultsPerPage number of results per page (handled by RapidAPI)
     * @return paginated job search results
     */
    /**
     * Maximum length for keyword/location query parameters to defend against
     * excessively long inputs that could cause issues with the external API.
     */
    private static final int MAX_QUERY_PARAM_LENGTH = 200;

    public JobSearchResultResponse searchJobs(String keyword, String location, int page, int resultsPerPage) {
        String apiKey = properties.getJsearch().getApiKey();

        // Validate API key
        validateApiKey(apiKey, "JSearch API Key");

        // Defense in depth: truncate keyword and location to a safe maximum length
        if (keyword != null && keyword.length() > MAX_QUERY_PARAM_LENGTH) {
            log.warn("Keyword exceeded max length ({}), truncating from {} to {} characters",
                    MAX_QUERY_PARAM_LENGTH, keyword.length(), MAX_QUERY_PARAM_LENGTH);
            keyword = keyword.substring(0, MAX_QUERY_PARAM_LENGTH);
        }
        if (location != null && location.length() > MAX_QUERY_PARAM_LENGTH) {
            log.warn("Location exceeded max length ({}), truncating from {} to {} characters",
                    MAX_QUERY_PARAM_LENGTH, location.length(), MAX_QUERY_PARAM_LENGTH);
            location = location.substring(0, MAX_QUERY_PARAM_LENGTH);
        }

        try {
            // Build the query string for JSearch specifically targeting internships
            String query = keyword + " intern";
            if (location != null && !location.isBlank()) {
                query += " in " + location;
            } else {
                query += " in Vietnam";
            }

            final String finalQuery = query;

            RestClient.RequestHeadersSpec<?> requestSpec = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/jsearch/search")
                            .queryParam("query", finalQuery)
                            .queryParam("page", page)
                            .queryParam("num_pages", 1) // Fetch 1 page at a time (usually ~10 results per page)
                            .queryParam("country", "vn") // Default searching country to Vietnam
                            .queryParam("employment_types", "INTERN") // Filter for internship type
                            .build())
                    .header("x-api-key", apiKey);

            String responseBody = requestSpec.retrieve().body(String.class);
            log.info("JSearch raw response: {}", responseBody);
            return parseJSearchResponse(responseBody, page, resultsPerPage);

        } catch (RestClientException e) {
            log.error("JSearch API call failed: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi goi JSearch API. Vui long lien he quan tri vien he thong.", e);
        }
    }

    /**
     * Validate API key has a reasonable format before making an external call.
     */
    private void validateApiKey(String key, String name) {
        if (key == null || key.isBlank() || key.equals("YOUR_RAPIDAPI_KEY")) {
            throw new IllegalArgumentException(name + " chua duoc cau hinh (dang dung gia tri mac dinh)");
        }
        if (key.length() < 10) {
            throw new IllegalArgumentException(name + " khong hop le (qua ngan)");
        }
    }

    private JobSearchResultResponse parseJSearchResponse(String responseBody, int page, int resultsPerPage) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Validate response: check for API-level error before attempting to parse data
            if (root.has("error") && !root.path("error").isNull()) {
                String errorMsg = root.path("error").asText("Unknown API error");
                log.error("JSearch API returned an error: {}", errorMsg);
                throw new ExternalApiException("JSearch API error: " + errorMsg);
            }

            // Also check for a "status" field indicating error (some APIs use this pattern)
            String status = root.path("status").asText("");
            if ("error".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)) {
                String errorMsg = root.path("message").asText("Unknown API error");
                log.error("JSearch API returned error status: {}", errorMsg);
                throw new ExternalApiException("JSearch API error: " + errorMsg);
            }

            JsonNode dataNode = root.path("data");
            List<ExternalJobResponse> jobs = new ArrayList<>();

            if (dataNode.isArray()) {
                for (JsonNode jobNode : dataNode) {
                    // Extract Salary
                    Double minSalary = jobNode.path("job_min_salary").isNull() ? null : jobNode.path("job_min_salary").asDouble();
                    Double maxSalary = jobNode.path("job_max_salary").isNull() ? null : jobNode.path("job_max_salary").asDouble();

                    // Location
                    String jobCity = jobNode.path("job_city").asText("");
                    String jobCountry = jobNode.path("job_country").asText("");
                    String location = jobCity.isEmpty() ? jobCountry : jobCity + ", " + jobCountry;

                    ExternalJobResponse job = ExternalJobResponse.builder()
                            .externalId(jobNode.path("job_id").asText())
                            .title(jobNode.path("job_title").asText())
                            .company(jobNode.path("employer_name").asText("Khong ro"))
                            .description(jobNode.path("job_description").asText())
                            .location(location)
                            .applyUrl(jobNode.path("job_apply_link").asText())
                            .salaryMin(minSalary)
                            .salaryMax(maxSalary)
                            .category(jobNode.path("job_employment_type").asText("N/A"))
                            .postedAt(parseDateTime(jobNode.path("job_posted_at_datetime_utc").asText()))
                            .build();
                    jobs.add(job);
                }
            }

            // JSearch (api.openwebninja.com) does not provide accurate total result counts
            // in its search response. Attempt to extract if available, otherwise use sentinel -1.
            int totalResults = extractTotalResults(root, page, resultsPerPage, jobs.size());
            int totalPages;
            if (totalResults == -1) {
                // Unknown total: show current page + 1 if this page has results
                totalPages = jobs.isEmpty() ? page : page + 1;
            } else {
                totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);
            }

            log.info("JSearch search returned {} results (page {})", jobs.size(), page);

            return JobSearchResultResponse.builder()
                    .jobs(jobs)
                    .totalResults(totalResults)
                    .currentPage(page)
                    .totalPages(totalPages)
                    .build();

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse JSearch response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly du lieu tu JSearch: " + e.getMessage(), e);
        }
    }

    /**
     * Attempt to extract total result count from JSearch API response.
     * JSearch (api.openwebninja.com) may not provide accurate totals.
     * Returns the extracted count if available, or -1 as a sentinel meaning "unknown".
     */
    private int extractTotalResults(JsonNode root, int page, int resultsPerPage, int jobsOnThisPage) {
        // Try known total-count fields used by JSearch variants
        String[] totalFieldNames = {"total", "totalResults", "total_count", "totalCount"};
        for (String fieldName : totalFieldNames) {
            if (root.has(fieldName) && !root.path(fieldName).isNull()) {
                int total = root.path(fieldName).asInt(-1);
                if (total >= 0) {
                    return total;
                }
            }
        }

        // Try metadata.total (nested object pattern)
        JsonNode metadata = root.path("metadata");
        if (!metadata.isMissingNode()) {
            for (String fieldName : totalFieldNames) {
                if (metadata.has(fieldName) && !metadata.path(fieldName).isNull()) {
                    int total = metadata.path(fieldName).asInt(-1);
                    if (total >= 0) {
                        return total;
                    }
                }
            }
        }

        // No total count available from the API — return sentinel -1.
        // Callers should treat -1 as "unknown total" and not rely on it for pagination UI.
        return -1;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equals("null")) {
            return null;
        }
        try {
            // JSearch returns UTC string e.g. "2023-11-24T12:00:00.000Z"
            // Use Instant which natively handles ISO-8601 with timezone offsets (including Z)
            Instant instant = Instant.parse(dateStr);
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return null;
        }
    }
}
