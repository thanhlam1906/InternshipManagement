package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.student.StudentCreateRequest;
import com.example.internshipmanagement.dto.request.student.StudentUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.student.StudentResponse;
import com.example.internshipmanagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> students = studentService.getAllStudents();

        ApiDataResponse<List<StudentResponse>> apiResponse = ApiDataResponse.<List<StudentResponse>>builder()
                .success(true)
                .message("Lay danh sach sinh vien thanh cong")
                .data(students)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDataResponse<StudentResponse>> getStudentById(@PathVariable Integer id) {
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
    public ResponseEntity<ApiDataResponse<StudentResponse>> updateStudent(
            @PathVariable("student_id") Integer studentId,
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
    public ResponseEntity<ApiDataResponse<Void>> deleteStudent(
            @PathVariable("student_id") Integer studentId) {
        studentService.deleteStudent(studentId);

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa thong tin sinh vien thanh cong")
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

