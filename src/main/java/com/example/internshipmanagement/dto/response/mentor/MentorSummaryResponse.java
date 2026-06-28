package com.example.internshipmanagement.dto.response.mentor;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSummaryResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String department;
    private String academicRank;
}

