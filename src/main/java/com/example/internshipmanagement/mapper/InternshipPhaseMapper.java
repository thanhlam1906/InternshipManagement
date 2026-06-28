package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.request.phase.InternshipPhaseCreateRequest;
import com.example.internshipmanagement.dto.request.phase.InternshipPhaseUpdateRequest;
import com.example.internshipmanagement.dto.response.phase.InternshipPhaseResponse;
import com.example.internshipmanagement.entity.InternshipPhase;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InternshipPhaseMapper {

    InternshipPhaseResponse toInternshipPhaseResponse(InternshipPhase internshipPhase);

    InternshipPhase toInternshipPhase(InternshipPhaseCreateRequest request);

    void updateInternshipPhase(@MappingTarget InternshipPhase internshipPhase, InternshipPhaseUpdateRequest request);
}

