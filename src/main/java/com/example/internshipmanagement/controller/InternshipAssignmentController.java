package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentCreateRequest;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentStatusUpdateRequest;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentUpdateRequest;
import com.example.internshipmanagement.dto.response.assignment.InternshipAssignmentResponse;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.service.InternshipAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internship_assignments")
@RequiredArgsConstructor
public class InternshipAssignmentController {

    private final InternshipAssignmentService internshipAssignmentService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<InternshipAssignmentResponse>>> getAssignments(
            @RequestParam(required = false) Integer phaseId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InternshipAssignmentResponse> assignmentsPage = internshipAssignmentService.getAssignments(phaseId, studentId, mentorId, pageable);

        PaginatedResponse<InternshipAssignmentResponse> data = PaginatedResponse.<InternshipAssignmentResponse>builder()
                .items(assignmentsPage.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(assignmentsPage.getNumber())
                        .pageSize(assignmentsPage.getSize())
                        .totalPages(assignmentsPage.getTotalPages())
                        .totalItems(assignmentsPage.getTotalElements())
                        .build())
                .build();

        return ResponseEntity.ok(ApiDataResponse.<PaginatedResponse<InternshipAssignmentResponse>>builder()
                .success(true)
                .message("Lấy danh sách phân công thực tập thành công")
                .data(data)
                .build());
    }

    @GetMapping("/{assignment_id}")
    public ResponseEntity<ApiDataResponse<InternshipAssignmentResponse>> getAssignmentById(
            @PathVariable("assignment_id") Integer id) {
        InternshipAssignmentResponse response = internshipAssignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiDataResponse.<InternshipAssignmentResponse>builder()
                .success(true)
                .message("Lấy chi tiết phân công thực tập thành công")
                .data(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiDataResponse<InternshipAssignmentResponse>> createAssignment(
            @Valid @RequestBody InternshipAssignmentCreateRequest request) {
        InternshipAssignmentResponse response = internshipAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiDataResponse.<InternshipAssignmentResponse>builder()
                        .success(true)
                        .message("Tạo phân công thực tập thành công")
                        .data(response)
                        .build());
    }

    @PutMapping("/{assignment_id}")
    public ResponseEntity<ApiDataResponse<InternshipAssignmentResponse>> updateAssignment(
            @PathVariable("assignment_id") Integer id,
            @Valid @RequestBody InternshipAssignmentUpdateRequest request) {
        InternshipAssignmentResponse response = internshipAssignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(ApiDataResponse.<InternshipAssignmentResponse>builder()
                .success(true)
                .message("Cập nhật phân công thực tập thành công")
                .data(response)
                .build());
    }

    @PutMapping("/{assignment_id}/status")
    public ResponseEntity<ApiDataResponse<InternshipAssignmentResponse>> updateAssignmentStatus(
            @PathVariable("assignment_id") Integer id,
            @Valid @RequestBody InternshipAssignmentStatusUpdateRequest request) {
        InternshipAssignmentResponse response = internshipAssignmentService.updateAssignmentStatus(id, request);
        return ResponseEntity.ok(ApiDataResponse.<InternshipAssignmentResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái phân công thực tập thành công")
                .data(response)
                .build());
    }

    @DeleteMapping("/{assignment_id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteAssignment(
            @PathVariable("assignment_id") Integer id) {
        internshipAssignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa phan cong thuc tap thanh cong")
                .data(null)
                .build());
    }
}
