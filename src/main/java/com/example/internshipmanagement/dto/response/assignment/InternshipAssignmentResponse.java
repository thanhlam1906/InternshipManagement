package com.example.internshipmanagement.dto.response.assignment;

import com.example.internshipmanagement.entity.enums.AssignmentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentResponse {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String studentCode;
    private Integer mentorId;
    private String mentorName;
    private Integer phaseId;
    private String phaseName;
    private LocalDateTime assignedDate;
    private AssignmentStatus status;
}
