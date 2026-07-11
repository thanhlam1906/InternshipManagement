package com.example.internshipmanagement.dto.response.evaluation_criterion;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriterionResponse {
    private Integer id;
    private String criterionName;
    private String description;
    private BigDecimal maxScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

