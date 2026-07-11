package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CVReviewService {

    /**
     * Upload a PDF CV, extract text, and send to Gemini AI for review.
     *
     * @param file         the uploaded PDF file
     * @param geminiApiKey the student's personal Gemini API key
     * @return AI-generated review feedback
     */
    CVReviewResponse reviewCV(MultipartFile file, String geminiApiKey);

    /**
     * Upload a PDF CV, extract text, and send to OpenAI for review.
     *
     * @param file        the uploaded PDF file
     * @param openAiApiKey the student's personal OpenAI API key (sk-...)
     * @return AI-generated review feedback
     */
    CVReviewResponse reviewCVWithOpenAi(MultipartFile file, String openAiApiKey);
}
