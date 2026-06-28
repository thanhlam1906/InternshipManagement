package com.example.internshipmanagement.dto.response.result;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResultResponse {
    private Integer id;
    private Integer assignmentId;
    private Integer roundId;
    private String roundName;
    private Integer criterionId;
    private String criterionName;
    private BigDecimal score;
    private String comments;
    private Integer evaluatedById;
    private String evaluatedByName;
    private LocalDateTime evaluationDate;
}
