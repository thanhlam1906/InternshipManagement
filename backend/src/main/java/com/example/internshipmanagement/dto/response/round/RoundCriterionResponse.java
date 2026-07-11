package com.example.internshipmanagement.dto.response.round;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundCriterionResponse {
    private Integer id;
    private Integer roundId;
    private Integer criterionId;
    private String criterionName;
    private BigDecimal maxScore;
    private BigDecimal weight;
}
