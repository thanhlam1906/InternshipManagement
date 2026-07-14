package com.example.internshipmanagement.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(min = 3, max = 50, message = "Ten dang nhap phai tu 3 den 50 ky tu")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Ten dang nhap chi duoc chua chu cai, so, dau gach duoi va dau gach ngang")
    private String username;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, max = 100, message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message = "Ho va ten khong duoc de trong")
    @Size(max = 100, message = "Ho va ten toi da 100 ky tu")
    private String fullName;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Dinh dang email khong hop le")
    @Size(max = 100, message = "Email toi da 100 ky tu")
    private String email;

    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    private String phoneNumber;
}
