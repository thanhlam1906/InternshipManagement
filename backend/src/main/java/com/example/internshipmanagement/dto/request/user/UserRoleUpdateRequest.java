package com.example.internshipmanagement.dto.request.user;

import com.example.internshipmanagement.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequest {

    @NotNull(message = "Vai tro khong duoc de trong")
    private Role role;
}

