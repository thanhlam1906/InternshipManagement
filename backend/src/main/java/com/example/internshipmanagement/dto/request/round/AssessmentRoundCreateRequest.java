package com.example.internshipmanagement.dto.request.round;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRoundCreateRequest {

    @NotNull(message = "Phase ID khong duoc de trong")
    private Integer phaseId;

    @NotBlank(message = "Ten vong danh gia khong duoc de trong")
    @Size(max = 100, message = "Ten vong danh gia toi da 100 ky tu")
    private String roundName;

    @NotNull(message = "Ngay bat dau khong duoc de trong")
    private LocalDate startDate;

    @NotNull(message = "Ngay ket thuc khong duoc de trong")
    private LocalDate endDate;

    private String description;

    @NotEmpty(message = "Danh sach tieu chi khong duoc de trong")
    @Valid
    private List<RoundCriterionRequest> criteria;

    @AssertTrue(message = "startDate must be before endDate")
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
}
