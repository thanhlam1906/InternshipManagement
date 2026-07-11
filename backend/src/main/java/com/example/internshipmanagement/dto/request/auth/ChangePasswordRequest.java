package com.example.internshipmanagement.dto.request.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mat khau hien tai khong duoc de trong")
    private String currentPassword;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 6, message = "Mat khau moi phai co it nhat 6 ky tu")
    private String newPassword;

    @NotBlank(message = "Xac nhan mat khau khong duoc de trong")
    private String confirmPassword;

    @AssertTrue(message = "newPassword must match confirmPassword")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
