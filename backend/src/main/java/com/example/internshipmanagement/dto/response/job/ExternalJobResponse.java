package com.example.internshipmanagement.dto.response.job;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalJobResponse {

    private String externalId;

    private String title;

    private String company;

    private String description;

    private String location;

    private String applyUrl;

    private Double salaryMin;

    private Double salaryMax;

    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime postedAt;
}
