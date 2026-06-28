package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.response.assignment.InternshipAssignmentResponse;
import com.example.internshipmanagement.entity.InternshipAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InternshipAssignmentMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.fullName", target = "studentName")
    @Mapping(source = "student.studentCode", target = "studentCode")
    @Mapping(source = "mentor.id", target = "mentorId")
    @Mapping(source = "mentor.user.fullName", target = "mentorName")
    @Mapping(source = "phase.id", target = "phaseId")
    @Mapping(source = "phase.phaseName", target = "phaseName")
    InternshipAssignmentResponse toResponse(InternshipAssignment entity);
}
