package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.response.dashboard.DashboardResponse;
import com.example.internshipmanagement.repository.IInternshipAssignmentRepository;
import com.example.internshipmanagement.repository.IMentorRepository;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IUserRepository userRepository;
    private final IStudentRepository studentRepository;
    private final IMentorRepository mentorRepository;
    private final IInternshipAssignmentRepository assignmentRepository;

    @Override
    public DashboardResponse getDashboardStats() {
        Long totalUsers = userRepository.count();
        Long totalStudents = studentRepository.count();
        Long totalMentors = mentorRepository.count();
        Long totalAssignments = assignmentRepository.count();

        // Build status map
        Map<String, Long> assignmentsByStatus = new LinkedHashMap<>();
        assignmentsByStatus.put("PENDING", 0L);
        assignmentsByStatus.put("IN_PROGRESS", 0L);
        assignmentsByStatus.put("COMPLETED", 0L);
        assignmentsByStatus.put("CANCELLED", 0L);

        assignmentRepository.countByStatus().forEach(row -> {
            String status = row[0].toString();
            Long count = (Long) row[1];
            assignmentsByStatus.put(status, count);
        });

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalMentors(totalMentors)
                .totalAssignments(totalAssignments)
                .assignmentsByStatus(assignmentsByStatus)
                .build();
    }
}
