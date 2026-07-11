package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.evaluation_criterion.EvaluationCriterionResponse;
import com.example.internshipmanagement.service.EvaluationCriterionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation-criteria")
@RequiredArgsConstructor
public class EvaluationCriterionController {

    private final EvaluationCriterionService service;

    @GetMapping
    public ResponseEntity<ApiDataResponse<List<EvaluationCriterionResponse>>> getAllCriteria() {
        List<EvaluationCriterionResponse> criteria = service.getAllCriteria();

        ApiDataResponse<List<EvaluationCriterionResponse>> apiResponse = ApiDataResponse.<List<EvaluationCriterionResponse>>builder()
                .success(true)
                .message("Lay danh sach tieu chi danh gia thanh cong")
                .data(criteria)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{criterion_id}")
    public ResponseEntity<ApiDataResponse<EvaluationCriterionResponse>> getCriterionById(
            @PathVariable("criterion_id") Integer criterionId) {
        EvaluationCriterionResponse criterion = service.getCriterionById(criterionId);

        ApiDataResponse<EvaluationCriterionResponse> apiResponse = ApiDataResponse.<EvaluationCriterionResponse>builder()
                .success(true)
                .message("Lay thong tin chi tiet tieu chi danh gia thanh cong")
                .data(criterion)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiDataResponse<EvaluationCriterionResponse>> createCriterion(
            @Valid @RequestBody EvaluationCriterionCreateRequest request) {
        EvaluationCriterionResponse criterion = service.createCriterion(request);

        ApiDataResponse<EvaluationCriterionResponse> apiResponse = ApiDataResponse.<EvaluationCriterionResponse>builder()
                .success(true)
                .message("Tao tieu chi danh gia moi thanh cong")
                .data(criterion)
                .httpStatus(HttpStatus.CREATED)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{criterion_id}")
    public ResponseEntity<ApiDataResponse<EvaluationCriterionResponse>> updateCriterion(
            @PathVariable("criterion_id") Integer criterionId,
            @Valid @RequestBody EvaluationCriterionUpdateRequest request) {
        EvaluationCriterionResponse criterion = service.updateCriterion(criterionId, request);

        ApiDataResponse<EvaluationCriterionResponse> apiResponse = ApiDataResponse.<EvaluationCriterionResponse>builder()
                .success(true)
                .message("Cap nhat thong tin tieu chi danh gia thanh cong")
                .data(criterion)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{criterion_id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteCriterion(
            @PathVariable("criterion_id") Integer criterionId) {
        service.deleteCriterion(criterionId);

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa tieu chi danh gia thanh cong")
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

