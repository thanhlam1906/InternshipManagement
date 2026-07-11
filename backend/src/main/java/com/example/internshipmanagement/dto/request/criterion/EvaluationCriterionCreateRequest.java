package com.example.internshipmanagement.dto.request.criterion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriterionCreateRequest {

    @NotBlank(message = "Ten tieu chi khong duoc de trong")
    @Size(max = 200, message = "Ten tieu chi toi da 200 ky tu")
    private String criterionName;

    private String description;

    @NotNull(message = "Diem toi da khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Diem toi da phai lon hon 0")
    private BigDecimal maxScore;
}

