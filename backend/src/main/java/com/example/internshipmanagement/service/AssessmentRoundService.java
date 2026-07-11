package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.round.AssessmentRoundCreateRequest;
import com.example.internshipmanagement.dto.request.round.AssessmentRoundUpdateRequest;
import com.example.internshipmanagement.dto.response.round.AssessmentRoundResponse;
import com.example.internshipmanagement.entity.AssessmentRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssessmentRoundService {
    Page<AssessmentRoundResponse> getAssessmentRounds(Integer phase_id,Pageable pageable);
    AssessmentRoundResponse getAssessmentRoundById(Integer id);
    AssessmentRoundResponse createAssessmentRound(AssessmentRoundCreateRequest request);
    AssessmentRoundResponse updateAssessmentRound(Integer id, AssessmentRoundUpdateRequest request);
    void deleteAssessmentRound(Integer id);
}
