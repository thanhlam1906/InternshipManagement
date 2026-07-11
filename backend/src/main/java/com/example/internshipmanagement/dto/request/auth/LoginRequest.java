package com.example.internshipmanagement.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    private String username;

    @NotBlank(message = "Mat khau khong duoc de trong")
    private String password;
}

