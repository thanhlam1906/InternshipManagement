package com.example.internshipmanagement.dto.request.assignment;

import com.example.internshipmanagement.entity.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentStatusUpdateRequest {
    @NotNull(message = "Trang thai khong duoc de trong")
    private AssignmentStatus status;
}
