package com.example.internshipmanagement.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Ho va ten khong duoc de trong")
    @Size(max = 100, message = "Ho va ten toi da 100 ky tu")
    private String fullName;

    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    private String phoneNumber;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Dinh dang email khong hop le")
    @Size(max = 100, message = "Email toi da 100 ky tu")
    private String email;
}

