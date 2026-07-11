package com.example.internshipmanagement.dto.response.job;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchResultResponse {

    private List<ExternalJobResponse> jobs;

    private Integer totalResults;

    private Integer currentPage;

    private Integer totalPages;
}
