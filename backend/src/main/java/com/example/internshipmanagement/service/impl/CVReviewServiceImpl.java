package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.config.RateLimiter;
import com.example.internshipmanagement.dto.response.cv.CVReviewResponse;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.service.CVReviewService;
import com.example.internshipmanagement.service.PDFExtractorService;
import com.example.internshipmanagement.service.client.GeminiClient;
import com.example.internshipmanagement.service.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CVReviewServiceImpl implements CVReviewService {

    private final PDFExtractorService pdfExtractorService;
    private final GeminiClient geminiClient;
    private final OpenAiClient openAiClient;
    private final IStudentRepository studentRepository;
    private final RateLimiter rateLimiter;

    @Override
    public CVReviewResponse reviewCV(MultipartFile file, String geminiApiKey) {
        // 0. Rate limit check
        Integer userId = getCurrentUserId();
        rateLimiter.checkCVReviewLimit(userId);

        // 1. Get current student's major
        String major = getCurrentStudentMajor(userId);
        log.info("Starting CV review for student userId={} with major: {}", userId, major);

        // 2. Extract text from PDF (in-memory, no file saved)
        String cvText = pdfExtractorService.extractText(file);
        log.info("Extracted {} characters from CV PDF", cvText.length());

        // 3. Send to Gemini AI for review
        CVReviewResponse review = geminiClient.reviewCV(geminiApiKey, cvText, major);
        log.info("CV review completed. Overall score: {}/10", review.getOverallScore());

        return review;
    }

    @Override
    public CVReviewResponse reviewCVWithOpenAi(MultipartFile file, String openAiApiKey) {
        // 0. Rate limit check
        Integer userId = getCurrentUserId();
        rateLimiter.checkCVReviewLimit(userId);

        // 1. Get current student's major
        String major = getCurrentStudentMajor(userId);
        log.info("Starting CV review (OpenAI) for student userId={} with major: {}", userId, major);

        // 2. Extract text from PDF (in-memory, no file saved)
        String cvText = pdfExtractorService.extractText(file);
        log.info("Extracted {} characters from CV PDF", cvText.length());

        // 3. Send to OpenAI for review
        CVReviewResponse review = openAiClient.reviewCV(openAiApiKey, cvText, major);
        log.info("CV review (OpenAI) completed. Overall score: {}/10", review.getOverallScore());

        return review;
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    /**
     * Get the major of the currently logged-in student.
     */
    private String getCurrentStudentMajor(Integer userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay thong tin sinh vien voi id: " + userId));

        String major = student.getMajor();
        if (major == null || major.isBlank()) {
            return "General"; // Default if major not set
        }

        return major;
    }
}
