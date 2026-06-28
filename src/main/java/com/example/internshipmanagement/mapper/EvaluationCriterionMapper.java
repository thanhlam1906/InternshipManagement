package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.evaluation_criterion.EvaluationCriterionResponse;
import com.example.internshipmanagement.entity.EvaluationCriterion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EvaluationCriterionMapper {

    EvaluationCriterionResponse toResponse(EvaluationCriterion criterion);

    EvaluationCriterion toEntity(EvaluationCriterionCreateRequest request);

    void updateEntity(@MappingTarget EvaluationCriterion criterion, EvaluationCriterionUpdateRequest request);
}

