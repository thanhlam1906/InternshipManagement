package com.example.internshipmanagement.dto.request.round;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundCriterionCreateRequest {
    @NotNull(message = "Round ID khong duoc de trong")
    private Integer roundId;

    @NotNull(message = "Criterion ID khong duoc de trong")
    private Integer criterionId;

    @NotNull(message = "Trong so khong duoc de trong")
    private BigDecimal weight;
}
