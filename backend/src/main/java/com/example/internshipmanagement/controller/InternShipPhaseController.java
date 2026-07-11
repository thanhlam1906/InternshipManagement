package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.phase.InternshipPhaseCreateRequest;
import com.example.internshipmanagement.dto.request.phase.InternshipPhaseUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.phase.InternshipPhaseResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.service.InternshipPhaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internship-phases")
@RequiredArgsConstructor
public class InternShipPhaseController {

    private final InternshipPhaseService internshipPhaseService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<InternshipPhaseResponse>>> getAllInternshipPhases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InternshipPhaseResponse> phases = internshipPhaseService.getAllIntershipPhase(pageable);

        PaginatedResponse<InternshipPhaseResponse> data = PaginatedResponse.<InternshipPhaseResponse>builder()
                .items(phases.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(phases.getNumber())
                        .pageSize(phases.getSize())
                        .totalPages(phases.getTotalPages())
                        .totalItems(phases.getTotalElements())
                        .build())
                .build();

        ApiDataResponse<PaginatedResponse<InternshipPhaseResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<InternshipPhaseResponse>>builder()
                .success(true)
                .message("Lay danh sach dot thuc tap thanh cong")
                .data(data)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDataResponse<InternshipPhaseResponse>> getInternshipPhaseById(
            @PathVariable("id") Integer id) {
        InternshipPhaseResponse phase = internshipPhaseService.getInternshipPhaseById(id);

        ApiDataResponse<InternshipPhaseResponse> apiResponse = ApiDataResponse.<InternshipPhaseResponse>builder()
                .success(true)
                .message("Lay thong tin chi tiet dot thuc tap thanh cong")
                .data(phase)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiDataResponse<InternshipPhaseResponse>> createInternshipPhase(
            @Valid @RequestBody InternshipPhaseCreateRequest request) {
        InternshipPhaseResponse phase = internshipPhaseService.createInternshipPhase(request);

        ApiDataResponse<InternshipPhaseResponse> apiResponse = ApiDataResponse.<InternshipPhaseResponse>builder()
                .success(true)
                .message("Tao dot thuc tap moi thanh cong")
                .data(phase)
                .httpStatus(HttpStatus.CREATED)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiDataResponse<InternshipPhaseResponse>> updateInternshipPhase(
            @PathVariable("id") Integer id,
            @Valid @RequestBody InternshipPhaseUpdateRequest request) {
        InternshipPhaseResponse phase = internshipPhaseService.updateInternshipPhase(id, request);

        ApiDataResponse<InternshipPhaseResponse> apiResponse = ApiDataResponse.<InternshipPhaseResponse>builder()
                .success(true)
                .message("Cap nhat thong tin dot thuc tap thanh cong")
                .data(phase)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteInternshipPhase(
            @PathVariable("id") Integer id) {
        internshipPhaseService.deleteInternshipPhase(id);

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa dot thuc tap thanh cong")
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

