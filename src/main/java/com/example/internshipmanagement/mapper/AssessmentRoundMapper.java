package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.request.round.AssessmentRoundCreateRequest;
import com.example.internshipmanagement.dto.request.round.AssessmentRoundUpdateRequest;
import com.example.internshipmanagement.dto.response.round.AssessmentRoundResponse;
import com.example.internshipmanagement.entity.AssessmentRound;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {RoundCriterionMapper.class})
public interface AssessmentRoundMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phase", ignore = true) // Will be set in service
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roundCriteria", ignore = true)
    AssessmentRound toEntity(AssessmentRoundCreateRequest request);

    @Mapping(source = "phase.id", target = "phaseId")
    @Mapping(source = "roundCriteria", target = "criteria")
    AssessmentRoundResponse toResponse(AssessmentRound entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phase", ignore = true) // Will be set in service if phaseId is provided
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roundCriteria", ignore = true)
    void updateEntity(@MappingTarget AssessmentRound entity, AssessmentRoundUpdateRequest request);
}

