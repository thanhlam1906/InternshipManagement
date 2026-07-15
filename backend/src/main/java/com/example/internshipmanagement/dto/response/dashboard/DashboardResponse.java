package com.example.internshipmanagement.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private Long totalUsers;
    private Long totalStudents;
    private Long totalMentors;
    private Long totalAssignments;
    private Map<String, Long> assignmentsByStatus;
}
