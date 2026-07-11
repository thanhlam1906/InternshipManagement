package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.response.round.RoundCriterionResponse;
import com.example.internshipmanagement.entity.RoundCriterion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoundCriterionMapper {

    @Mapping(source = "round.id", target = "roundId")
    @Mapping(source = "criterion.id", target = "criterionId")
    @Mapping(source = "criterion.criterionName", target = "criterionName")
    @Mapping(source = "criterion.maxScore", target = "maxScore")
    RoundCriterionResponse toResponse(RoundCriterion entity);
}
