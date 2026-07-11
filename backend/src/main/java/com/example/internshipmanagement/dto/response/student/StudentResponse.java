package com.example.internshipmanagement.dto.response.student;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Integer id;
    private String studentCode;
    private String major;
    private String clazz;
    private LocalDate dateOfBirth;
    private String address;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

