package com.example.internshipmanagement.dto.request.phase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.w3c.dom.Text;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipPhaseCreateRequest {

    @NotBlank(message = "Ten dot thuc tap khong duoc de trong")
    @Size(max = 100, message = "Ten dot thuc tap toi da 100 ky tu")
    private String phaseName;

    @NotNull(message = "Ngay bat dau khong duoc de trong")
    private LocalDate startDate;


    @NotNull(message = "Ngay ket thuc khong duoc de trong")
    private LocalDate endDate;

    private String description;
}

