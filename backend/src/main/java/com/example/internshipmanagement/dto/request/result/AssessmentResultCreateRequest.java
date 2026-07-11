package com.example.internshipmanagement.dto.request.result;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResultCreateRequest {
    @NotNull(message = "Assignment ID khong duoc de trong")
    private Integer assignmentId;

    @NotNull(message = "Round ID khong duoc de trong")
    private Integer roundId;

    @NotNull(message = "Criterion ID khong duoc de trong")
    private Integer criterionId;

    @NotNull(message = "Diem so khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Diem so phai lon hon hoac bang 0")
    private BigDecimal score;

    private String comments;
}
