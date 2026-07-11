package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.mentor.MentorCreateRequest;
import com.example.internshipmanagement.dto.request.mentor.MentorUpdateRequest;
import com.example.internshipmanagement.dto.response.mentor.MentorResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorSummaryResponse;
import com.example.internshipmanagement.entity.Mentor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MentorService {
    List<MentorResponse> getAllMentors();
    List<MentorSummaryResponse> getAllMentorsSummary();
    Page<MentorResponse> getAllMentors(Pageable pageable);
    Page<MentorSummaryResponse> getAllMentorsSummary(Pageable pageable);
    MentorResponse getMentorById(Integer id);
    MentorResponse createMentor(MentorCreateRequest request);
    MentorResponse updateMentor(Integer mentorId ,MentorUpdateRequest request);
    void deleteMentor(Integer id);
}

