package com.example.internshipmanagement.dto.request.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentUpdateRequest {
    @NotNull(message = "Student ID khong duoc de trong")
    private Integer studentId;

    @NotNull(message = "Mentor ID khong duoc de trong")
    private Integer mentorId;

    @NotNull(message = "Phase ID khong duoc de trong")
    private Integer phaseId;
}
