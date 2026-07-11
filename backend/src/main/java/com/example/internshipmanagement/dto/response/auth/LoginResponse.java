package com.example.internshipmanagement.dto.response.auth;

import com.example.internshipmanagement.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Integer userId;
    private String username;
    private String fullName;
    private Role role;
}

