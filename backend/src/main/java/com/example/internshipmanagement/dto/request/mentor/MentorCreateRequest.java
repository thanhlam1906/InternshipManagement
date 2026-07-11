package com.example.internshipmanagement.dto.request.mentor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorCreateRequest {
    @NotNull(message = "User ID khong duoc de trong")
    private Integer userId;
    @NotBlank(message= "Department khong duoc de trong")
    @Size(max = 100, message = "Nhap toi da 100 ki tu")
    private String department;
    @NotBlank(message= "Hoc vi khong duoc de trong")
    @Size(max = 50, message = "Nhap toi da 50 ki tu")
    private String academicRank;


}

