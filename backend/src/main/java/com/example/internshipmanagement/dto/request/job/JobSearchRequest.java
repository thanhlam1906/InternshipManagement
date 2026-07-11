package com.example.internshipmanagement.dto.request.job;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchRequest {

    @Size(max = 200, message = "Tu khoa toi da 200 ky tu")
    private String keyword;

    @Size(max = 200, message = "Dia diem toi da 200 ky tu")
    private String location;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    @Min(value = 1, message = "Kich thuoc trang toi thieu la 1")
    @Max(value = 100, message = "Kich thuoc trang toi da la 100")
    private Integer pageSize = 10;
}
