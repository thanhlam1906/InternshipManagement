package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.student.StudentCreateRequest;
import com.example.internshipmanagement.dto.request.student.StudentUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.dto.response.student.StudentResponse;
import com.example.internshipmanagement.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiDataResponse<PaginatedResponse<StudentResponse>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> studentPage = studentService.getAllStudents(pageable);

        PaginatedResponse<StudentResponse> data = PaginatedResponse.<StudentResponse>builder()
                .items(studentPage.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(studentPage.getNumber())
                        .pageSize(studentPage.getSize())
                        .totalPages(studentPage.getTotalPages())
                        .totalItems(studentPage.getTotalElements())
                        .build())
                .build();

        ApiDataResponse<PaginatedResponse<StudentResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<StudentResponse>>builder()
                .success(true)
                .message("Lay danh sach sinh vien thanh cong")
                .data(data)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public ResponseEntity<ApiDataResponse<StudentResponse>> getStudentById(@PathVariable @Positive(message = "ID must be positive") Integer id) {
        StudentResponse student = studentService.getStudentById(id);

        ApiDataResponse<StudentResponse> apiResponse = ApiDataResponse.<StudentResponse>builder()
                .success(true)
                .message("Lay thong tin sinh vien thanh cong")
                .data(student)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        StudentResponse response = studentService.createStudent(request);

        ApiDataResponse<StudentResponse> apiResponse = ApiDataResponse.<StudentResponse>builder()
                .success(true)
                .message("Tao thong tin sinh vien thanh cong")
                .data(response)
                .httpStatus(HttpStatus.CREATED)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{student_id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiDataResponse<StudentResponse>> updateStudent(
            @PathVariable("student_id") @Positive(message = "ID must be positive") Integer studentId,
            @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse response = studentService.updateStudent(studentId, request);

        ApiDataResponse<StudentResponse> apiResponse = ApiDataResponse.<StudentResponse>builder()
                .success(true)
                .message("Cap nhat thong tin sinh vien thanh cong")
                .data(response)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{student_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable("student_id") @Positive(message = "ID must be positive") Integer studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}

