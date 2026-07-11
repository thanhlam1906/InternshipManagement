package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.config.RateLimiter;
import com.example.internshipmanagement.dto.request.job.JobSearchRequest;
import com.example.internshipmanagement.dto.response.job.JobSearchResultResponse;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.service.JobSearchService;
import com.example.internshipmanagement.service.client.JSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchServiceImpl implements JobSearchService {

    private final JSearchClient jSearchClient;
    private final IStudentRepository studentRepository;
    private final RateLimiter rateLimiter;

    @Override
    public JobSearchResultResponse searchJobs(JobSearchRequest request) {
        // Rate limit check
        Integer userId = getCurrentUserId();
        rateLimiter.checkJobSearchLimit(userId);

        String keyword = request.getKeyword();

        // If no keyword provided, use the student's major
        if (keyword == null || keyword.isBlank()) {
            keyword = getCurrentStudentMajor(userId);
        }

        int page = request.getPage() != null ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;

        log.info("Searching jobs: userId={}, keyword='{}', location='{}', page={}, pageSize={}",
                userId, keyword, request.getLocation(), page, pageSize);

        return jSearchClient.searchJobs(keyword, request.getLocation(), page, pageSize);
    }

    @Override
    public JobSearchResultResponse searchJobsByMajor(Integer page, Integer pageSize) {
        // Rate limit check
        Integer userId = getCurrentUserId();
        rateLimiter.checkJobSearchLimit(userId);

        String major = getCurrentStudentMajor(userId);

        log.info("Searching jobs by major: userId={}, major='{}', page={}, pageSize={}",
                userId, major, page, pageSize);

        return jSearchClient.searchJobs(major, null,
                page != null ? page : 1,
                pageSize != null ? pageSize : 10);
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    /**
     * Get the major of the currently logged-in student.
     */
    private String getCurrentStudentMajor(Integer userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay thong tin sinh vien voi id: " + userId));

        String major = student.getMajor();
        if (major == null || major.isBlank()) {
            throw new IllegalArgumentException(
                    "Sinh vien chua cap nhat nganh hoc. Vui long cap nhat thong tin ca nhan truoc.");
        }

        return major;
    }
}
