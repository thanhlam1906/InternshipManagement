package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.result.AssessmentResultCreateRequest;
import com.example.internshipmanagement.dto.request.result.AssessmentResultUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.dto.response.result.AssessmentResultResponse;
import com.example.internshipmanagement.service.AssessmentResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment_results")
@RequiredArgsConstructor
public class AssessmentResultController {

    private final AssessmentResultService assessmentResultService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<AssessmentResultResponse>>> getAssessmentResults(
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer mentorId,
            @RequestParam(required = false) Integer roundId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssessmentResultResponse> resultsPage = assessmentResultService.getAssessmentResults(assignmentId, studentId, mentorId, roundId, pageable);

        PaginatedResponse<AssessmentResultResponse> data = PaginatedResponse.<AssessmentResultResponse>builder()
                .items(resultsPage.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(resultsPage.getNumber())
                        .pageSize(resultsPage.getSize())
                        .totalPages(resultsPage.getTotalPages())
                        .totalItems(resultsPage.getTotalElements())
                        .build())
                .build();

        return ResponseEntity.ok(ApiDataResponse.<PaginatedResponse<AssessmentResultResponse>>builder()
                .success(true)
                .message("Lấy danh sách kết quả đánh giá thành công")
                .data(data)
                .build());
    }

    @GetMapping("/{result_id}")
    public ResponseEntity<ApiDataResponse<AssessmentResultResponse>> getAssessmentResultById(
            @PathVariable("result_id") Integer id) {
        AssessmentResultResponse response = assessmentResultService.getAssessmentResultById(id);
        return ResponseEntity.ok(ApiDataResponse.<AssessmentResultResponse>builder()
                .success(true)
                .message("Lấy chi tiết kết quả đánh giá thành công")
                .data(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiDataResponse<AssessmentResultResponse>> createAssessmentResult(
            @Valid @RequestBody AssessmentResultCreateRequest request) {
        AssessmentResultResponse response = assessmentResultService.createAssessmentResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiDataResponse.<AssessmentResultResponse>builder()
                        .success(true)
                        .message("Tạo kết quả đánh giá thành công")
                        .data(response)
                        .build());
    }

    @PutMapping("/{result_id}")
    public ResponseEntity<ApiDataResponse<AssessmentResultResponse>> updateAssessmentResult(
            @PathVariable("result_id") Integer id,
            @Valid @RequestBody AssessmentResultUpdateRequest request) {
        AssessmentResultResponse response = assessmentResultService.updateAssessmentResult(id, request);
        return ResponseEntity.ok(ApiDataResponse.<AssessmentResultResponse>builder()
                .success(true)
                .message("Cập nhật kết quả đánh giá thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{result_id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteAssessmentResult(
            @PathVariable("result_id") Integer id) {
        assessmentResultService.deleteAssessmentResult(id);
        return ResponseEntity.ok(ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xóa kết quả đánh giá thành công")
                .data(null)
                .build());
    }
}
