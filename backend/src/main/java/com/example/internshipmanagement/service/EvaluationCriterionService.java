package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.evaluation_criterion.EvaluationCriterionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EvaluationCriterionService {
    List<EvaluationCriterionResponse> getAllCriteria();
    Page<EvaluationCriterionResponse> getAllCriteria(Pageable pageable);
    EvaluationCriterionResponse getCriterionById(Integer id);
    EvaluationCriterionResponse createCriterion(EvaluationCriterionCreateRequest request);
    EvaluationCriterionResponse updateCriterion(Integer id, EvaluationCriterionUpdateRequest request);
    void deleteCriterion(Integer id);
}

