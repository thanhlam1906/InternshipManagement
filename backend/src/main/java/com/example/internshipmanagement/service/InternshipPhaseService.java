package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.phase.InternshipPhaseCreateRequest;
import com.example.internshipmanagement.dto.request.phase.InternshipPhaseUpdateRequest;
import com.example.internshipmanagement.dto.response.phase.InternshipPhaseResponse;
import com.example.internshipmanagement.entity.InternshipPhase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface InternshipPhaseService {
    Page<InternshipPhaseResponse> getAllIntershipPhase(Pageable pageable);
    InternshipPhaseResponse getInternshipPhaseById(Integer id);
    InternshipPhaseResponse createInternshipPhase(InternshipPhaseCreateRequest request);
    InternshipPhaseResponse updateInternshipPhase(Integer id, InternshipPhaseUpdateRequest request);
    void deleteInternshipPhase(Integer id);
}

