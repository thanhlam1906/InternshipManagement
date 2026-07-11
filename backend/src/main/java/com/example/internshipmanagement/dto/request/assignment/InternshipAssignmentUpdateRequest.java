package com.example.internshipmanagement.dto.request.assignment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentUpdateRequest {
    private Integer studentId;
    private Integer mentorId;
    private Integer phaseId;
}
