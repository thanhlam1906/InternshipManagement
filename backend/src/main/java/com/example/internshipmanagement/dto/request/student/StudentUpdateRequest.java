package com.example.internshipmanagement.dto.request.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentUpdateRequest {

    @NotBlank(message = "Ma sinh vien khong duoc de trong")
    @Size(max = 20, message = "Ma sinh vien toi da 20 ky tu")
    private String studentCode;

    @Size(max = 100, message = "Nganh hoc toi da 100 ky tu")
    private String major;

    @Size(max = 50, message = "Lop hoc toi da 50 ky tu")
    private String clazz;

    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Dia chi toi da 255 ky tu")
    private String address;
}

