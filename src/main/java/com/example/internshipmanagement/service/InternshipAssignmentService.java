package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentCreateRequest;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentStatusUpdateRequest;
import com.example.internshipmanagement.dto.response.assignment.InternshipAssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternshipAssignmentService {
    Page<InternshipAssignmentResponse> getAssignments(Integer phaseId, Pageable pageable);
    InternshipAssignmentResponse getAssignmentById(Integer id);
    InternshipAssignmentResponse createAssignment(InternshipAssignmentCreateRequest request);
    InternshipAssignmentResponse updateAssignment(Integer id, com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentUpdateRequest request);
    InternshipAssignmentResponse updateAssignmentStatus(Integer id, InternshipAssignmentStatusUpdateRequest request);
}
