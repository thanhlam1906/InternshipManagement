package com.example.internshipmanagement.dto.request.job;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchRequest {

    private String keyword;

    private String location;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer pageSize = 10;
}
