package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.student.StudentCreateRequest;
import com.example.internshipmanagement.dto.request.student.StudentUpdateRequest;
import com.example.internshipmanagement.dto.response.student.StudentResponse;
import com.example.internshipmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    List<StudentResponse> getAllStudents();
    Page<StudentResponse> getAllStudents(Pageable pageable);
    StudentResponse getStudentById(Integer id);
    StudentResponse createStudent(StudentCreateRequest request);
    StudentResponse updateStudent(Integer studentId, StudentUpdateRequest request);
    void deleteStudent(Integer id);
}

