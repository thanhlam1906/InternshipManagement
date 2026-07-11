package com.example.internshipmanagement.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequest {

    @NotNull(message = "Trang thai hoat dong khong duoc de trong")
    private Boolean isActive;
}

