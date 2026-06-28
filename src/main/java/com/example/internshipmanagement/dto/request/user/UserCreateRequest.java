package com.example.internshipmanagement.dto.request.user;

import com.example.internshipmanagement.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(min = 3, max = 50, message = "Ten dang nhap phai tu 3 den 50 ky tu")
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

    @NotNull(message = "Vai tro la bat buoc")
    private Role role;
}

