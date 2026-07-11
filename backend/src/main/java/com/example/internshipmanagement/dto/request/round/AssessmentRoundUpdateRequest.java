package com.example.internshipmanagement.dto.request.round;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRoundUpdateRequest {

    @Positive(message = "Phase ID phai la so duong")
    private Integer phaseId;

    @Size(max = 100, message = "Ten vong danh gia toi da 100 ky tu")
    private String roundName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @FutureOrPresent(message = "Ngay bat dau phai tu hien tai tro ve sau")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @FutureOrPresent(message = "Ngay ket thuc phai tu hien tai tro ve sau")
    private LocalDate endDate;

    private String description;

    private Boolean isActive;
}

