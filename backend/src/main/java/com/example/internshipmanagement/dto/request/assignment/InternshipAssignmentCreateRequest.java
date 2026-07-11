package com.example.internshipmanagement.dto.request.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignmentCreateRequest {
    @NotNull(message = "Sinh vien ID khong duoc de trong")
    private Integer studentId;

    @NotNull(message = "Giang vien ID khong duoc de trong")
    private Integer mentorId;

    @NotNull(message = "Giai doan thuc tap ID khong duoc de trong")
    private Integer phaseId;
}
