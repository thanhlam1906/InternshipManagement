package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.round.RoundCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.round.RoundCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.round.RoundCriterionResponse;
import com.example.internshipmanagement.service.RoundCriterionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/round_criteria")
@RequiredArgsConstructor
@Validated
public class RoundCriterionController {

    private final RoundCriterionService roundCriterionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiDataResponse<List<RoundCriterionResponse>>> getRoundCriteria(
            @RequestParam @Positive(message = "Round ID must be positive") Integer roundId) {
        List<RoundCriterionResponse> response = roundCriterionService.getCriteriaByRoundId(roundId);
        return ResponseEntity.ok(ApiDataResponse.<List<RoundCriterionResponse>>builder()
                .success(true)
                .message("Lấy danh sách tiêu chí của đợt đánh giá thành công")
                .data(response)
                .build());
    }

    @GetMapping("/{round_criterion_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiDataResponse<RoundCriterionResponse>> getRoundCriterionById(
            @PathVariable("round_criterion_id") @Positive(message = "ID must be positive") Integer id) {
        RoundCriterionResponse response = roundCriterionService.getRoundCriterionById(id);
        return ResponseEntity.ok(ApiDataResponse.<RoundCriterionResponse>builder()
                .success(true)
                .message("Lấy chi tiết tiêu chí của đợt đánh giá thành công")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<RoundCriterionResponse>> createRoundCriterion(
            @Valid @RequestBody RoundCriterionCreateRequest request) {
        RoundCriterionResponse response = roundCriterionService.createRoundCriterion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiDataResponse.<RoundCriterionResponse>builder()
                        .success(true)
                        .message("Thêm tiêu chí vào đợt đánh giá thành công")
                        .data(response)
                        .build());
    }

    @PutMapping("/{round_criterion_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<RoundCriterionResponse>> updateRoundCriterion(
            @PathVariable("round_criterion_id") @Positive(message = "ID must be positive") Integer id,
            @Valid @RequestBody RoundCriterionUpdateRequest request) {
        RoundCriterionResponse response = roundCriterionService.updateRoundCriterion(id, request);
        return ResponseEntity.ok(ApiDataResponse.<RoundCriterionResponse>builder()
                .success(true)
                .message("Cập nhật trọng số tiêu chí thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{round_criterion_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Void>> deleteRoundCriterion(
            @PathVariable("round_criterion_id") @Positive(message = "ID must be positive") Integer id) {
        roundCriterionService.deleteRoundCriterion(id);
        return ResponseEntity.ok(ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xóa tiêu chí khỏi đợt đánh giá thành công")
                .data(null)
                .build());
    }
}
