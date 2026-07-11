package com.example.internshipmanagement.dto.request.round;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRoundUpdateRequest {

    private Integer phaseId;

    @Size(max = 100, message = "Ten vong danh gia toi da 100 ky tu")
    private String roundName;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Boolean isActive;
}

