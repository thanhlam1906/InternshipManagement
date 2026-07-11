package com.example.internshipmanagement.dto.response.job;

import lombok.*;

import java.util.List;

/**
 * Paginated response for external job search results.
 *
 * <p><b>Important:</b> JSearch (api.openwebninja.com) does not provide accurate
 * total count information. When {@code totalResults} is {@code -1}, it means
 * the total is unknown. Clients should not rely on {@code totalPages} for
 * precise navigation when {@code totalResults} is {@code -1}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchResultResponse {

    private List<ExternalJobResponse> jobs;

    /**
     * Total number of search results, or -1 if the external API does not
     * provide an accurate count (sentinel value meaning "unknown").
     */
    private Integer totalResults;

    private Integer currentPage;

    /**
     * Estimated total pages. When {@code totalResults} is -1, this is a
     * best-guess value and should not be used for precise pagination controls.
     */
    private Integer totalPages;
}
