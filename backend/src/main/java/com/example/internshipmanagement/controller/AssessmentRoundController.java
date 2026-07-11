package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.round.AssessmentRoundCreateRequest;
import com.example.internshipmanagement.dto.request.round.AssessmentRoundUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.dto.response.round.AssessmentRoundResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.service.AssessmentRoundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment-rounds")
@RequiredArgsConstructor
public class AssessmentRoundController {
    private final AssessmentRoundService assessmentRoundService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<AssessmentRoundResponse>>> getAssessmentRounds(
            @RequestParam(required = false) Integer phaseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size


    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssessmentRoundResponse> roundPage = assessmentRoundService.getAssessmentRounds(phaseId, pageable);
        PaginatedResponse<AssessmentRoundResponse> data = PaginatedResponse.<AssessmentRoundResponse>builder()
                .items(roundPage.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(roundPage.getNumber())
                        .pageSize(roundPage.getSize())
                        .totalPages(roundPage.getTotalPages())
                        .totalItems(roundPage.getTotalElements())
                        .build())
                .build();

        ApiDataResponse<PaginatedResponse<AssessmentRoundResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<AssessmentRoundResponse>>builder()
                .success(true)
                .message("Lay danh sach user thanh cong")
                .data(data)
                .httpStatus(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDataResponse<AssessmentRoundResponse>> getAssessmentRoundById(@PathVariable Integer id) {
        AssessmentRoundResponse response = assessmentRoundService.getAssessmentRoundById(id);
        return ResponseEntity.ok(ApiDataResponse.<AssessmentRoundResponse>builder()
                .success(true)
                .message("Lấy thông tin đợt đánh giá thành công")
                .data(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiDataResponse<AssessmentRoundResponse>> createAssessmentRound(
            @Valid @RequestBody AssessmentRoundCreateRequest request) {
        AssessmentRoundResponse response = assessmentRoundService.createAssessmentRound(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiDataResponse.<AssessmentRoundResponse>builder()
                        .success(true)
                        .message("Tạo đợt đánh giá thành công")
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiDataResponse<AssessmentRoundResponse>> updateAssessmentRound(
            @PathVariable Integer id,
            @Valid @RequestBody AssessmentRoundUpdateRequest request) {
        AssessmentRoundResponse response = assessmentRoundService.updateAssessmentRound(id, request);
        return ResponseEntity.ok(ApiDataResponse.<AssessmentRoundResponse>builder()
                .success(true)
                .message("Cập nhật đợt đánh giá thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteAssessmentRound(@PathVariable Integer id) {
        assessmentRoundService.deleteAssessmentRound(id);
        return ResponseEntity.ok(ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xóa đợt đánh giá thành công")
                .data(null)
                .build());
    }
}
