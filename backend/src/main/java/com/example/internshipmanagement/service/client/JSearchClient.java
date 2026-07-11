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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public JobSearchResultResponse searchJobs(String keyword, String location, int page, int resultsPerPage) {
        String apiKey = properties.getJsearch().getApiKey();

        // Validate API key
        validateApiKey(apiKey, "JSearch API Key");

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

            // JSearch doesn't return exact total count, we simulate it based on page and data
            int totalResults = page * resultsPerPage + (jobs.size() == resultsPerPage ? 1 : 0);
            int totalPages = jobs.isEmpty() ? page : page + 1; // Simplistic approach since JSearch doesn't provide total

            log.info("JSearch search returned {} results", jobs.size());

            return JobSearchResultResponse.builder()
                    .jobs(jobs)
                    .totalResults(totalResults) // mock
                    .currentPage(page)
                    .totalPages(totalPages) // mock
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse JSearch response: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi xu ly du lieu tu JSearch: " + e.getMessage(), e);
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equals("null")) {
            return null;
        }
        try {
            // JSearch returns UTC string e.g. "2023-11-24T12:00:00.000Z"
            if (dateStr.endsWith("Z")) {
                dateStr = dateStr.substring(0, dateStr.length() - 1);
            }
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return null;
        }
    }
}
