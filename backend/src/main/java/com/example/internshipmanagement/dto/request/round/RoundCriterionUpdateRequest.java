package com.example.internshipmanagement.dto.request.round;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundCriterionUpdateRequest {
    @NotNull(message = "Trong so khong duoc de trong")
    private BigDecimal weight;
}
