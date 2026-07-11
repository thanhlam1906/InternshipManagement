package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.response.result.AssessmentResultResponse;
import com.example.internshipmanagement.entity.AssessmentResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentResultMapper {

    @Mapping(source = "assignment.id", target = "assignmentId")
    @Mapping(source = "round.id", target = "roundId")
    @Mapping(source = "round.roundName", target = "roundName")
    @Mapping(source = "criterion.id", target = "criterionId")
    @Mapping(source = "criterion.criterionName", target = "criterionName")
    @Mapping(source = "evaluatedBy.userId", target = "evaluatedById")
    @Mapping(source = "evaluatedBy.fullName", target = "evaluatedByName")
    AssessmentResultResponse toResponse(AssessmentResult entity);
}
