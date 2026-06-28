package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.result.AssessmentResultCreateRequest;
import com.example.internshipmanagement.dto.request.result.AssessmentResultUpdateRequest;
import com.example.internshipmanagement.dto.response.result.AssessmentResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssessmentResultService {
    Page<AssessmentResultResponse> getAssessmentResults(Integer assignmentId, Pageable pageable);
    AssessmentResultResponse createAssessmentResult(AssessmentResultCreateRequest request);
    AssessmentResultResponse updateAssessmentResult(Integer id, AssessmentResultUpdateRequest request);
}
