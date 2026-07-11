package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.job.JobSearchRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.job.JobSearchResultResponse;
import com.example.internshipmanagement.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobSearchController {

    private final JobSearchService jobSearchService;

    /**
     * Search for internship jobs with optional keyword and location filters.
     * If keyword is not provided, the student's major is used automatically.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiDataResponse<JobSearchResultResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        JobSearchRequest request = JobSearchRequest.builder()
                .keyword(keyword)
                .location(location)
                .page(page)
                .pageSize(pageSize)
                .build();

        JobSearchResultResponse result = jobSearchService.searchJobs(request);

        ApiDataResponse<JobSearchResultResponse> response = ApiDataResponse.<JobSearchResultResponse>builder()
                .success(true)
                .message("Tim kiem viec lam thuc tap thanh cong")
                .data(result)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search for internship jobs automatically based on the logged-in student's major.
     */
    @GetMapping("/search/by-major")
    public ResponseEntity<ApiDataResponse<JobSearchResultResponse>> searchJobsByMajor(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        JobSearchResultResponse result = jobSearchService.searchJobsByMajor(page, pageSize);

        ApiDataResponse<JobSearchResultResponse> response = ApiDataResponse.<JobSearchResultResponse>builder()
                .success(true)
                .message("Tim kiem viec lam theo nganh hoc thanh cong")
                .data(result)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
