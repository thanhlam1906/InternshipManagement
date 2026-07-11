package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.job.JobSearchRequest;
import com.example.internshipmanagement.dto.response.job.JobSearchResultResponse;

public interface JobSearchService {

    /**
     * Search for internship jobs using external Adzuna API.
     * If keyword is not provided, the student's major is used automatically.
     *
     * @param request      search parameters (keyword, location, page, pageSize)
     * @return paginated list of jobs from external API
     */
    JobSearchResultResponse searchJobs(JobSearchRequest request);

    /**
     * Search for internship jobs automatically based on the logged-in student's major.
     *
     * @param page         page number
     * @param pageSize     results per page
     * @return paginated list of jobs matching the student's major
     */
    JobSearchResultResponse searchJobsByMajor(Integer page, Integer pageSize);
}
