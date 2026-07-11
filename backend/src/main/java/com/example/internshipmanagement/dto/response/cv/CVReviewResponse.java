package com.example.internshipmanagement.dto.response.cv;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVReviewResponse {

    private Integer overallScore;

    private String summary;

    private String formatFeedback;

    private String contentFeedback;

    private String strengthPoints;

    private String improvementSuggestions;

    private String majorRelevance;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt = LocalDateTime.now();
}
