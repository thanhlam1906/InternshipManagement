package com.example.internshipmanagement.dto.response.mentor;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorResponse {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private String department;
    private String academicRank;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

