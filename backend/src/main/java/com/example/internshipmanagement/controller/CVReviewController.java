package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.service.CVReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVReviewController {

    private final CVReviewService cvReviewService;

    /**
     * Upload a PDF CV and receive AI-powered review feedback.
     * The PDF is processed in-memory (not saved to disk or database).
     *
     * Required header:
     * - X-Gemini-Api-Key: the student's personal Google Gemini API key
     *
     * @param file         PDF file (max 10MB)
     * @param geminiApiKey Gemini API key from header
     * @return AI review with score, feedback, and suggestions
     */
    @PostMapping(value = "/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiDataResponse<CVReviewResponse>> reviewCV(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Gemini-Api-Key") String geminiApiKey) {

        CVReviewResponse review = cvReviewService.reviewCV(file, geminiApiKey);

        ApiDataResponse<CVReviewResponse> response = ApiDataResponse.<CVReviewResponse>builder()
                .success(true)
                .message("Danh gia CV thanh cong")
                .data(review)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Upload a PDF CV and receive AI-powered review feedback via Groq (OpenAI-compatible, free tier).
     * The PDF is processed in-memory (not saved to disk or database).
     *
     * Required header:
     * - X-OpenAI-Api-Key: the student's personal Groq API key (gsk_...)
     *
     * @param file         PDF file (max 10MB)
     * @param openAiApiKey Groq API key from header
     * @return AI review with score, feedback, and suggestions
     */
    @PostMapping(value = "/review/openai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiDataResponse<CVReviewResponse>> reviewCVWithOpenAi(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-OpenAI-Api-Key") String openAiApiKey) {

        CVReviewResponse review = cvReviewService.reviewCVWithOpenAi(file, openAiApiKey);

        ApiDataResponse<CVReviewResponse> response = ApiDataResponse.<CVReviewResponse>builder()
                .success(true)
                .message("Danh gia CV thanh cong (OpenAI)")
                .data(review)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
