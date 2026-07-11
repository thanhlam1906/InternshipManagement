package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.dto.request.student.StudentCreateRequest;
import com.example.internshipmanagement.dto.request.student.StudentUpdateRequest;
import com.example.internshipmanagement.dto.response.student.StudentResponse;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.mapper.StudentMapper;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.repository.IInternshipAssignmentRepository;
import com.example.internshipmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final IStudentRepository studentRepository;
    private final IUserRepository userRepository;
    private final StudentMapper studentMapper;
    private final IInternshipAssignmentRepository internshipAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();

        List<Student> students;
        if (role == Role.ADMIN) {
            students = studentRepository.findAll();
        } else if (role == Role.MENTOR) {
            students = studentRepository.findStudentsByMentorId(userId);
        } else {
            throw new IllegalArgumentException("Khong co quyen truy cap");
        }

        return students.stream()
                .map(studentMapper::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();

        Page<Student> studentPage;
        if (role == Role.ADMIN) {
            studentPage = studentRepository.findAll(pageable);
        } else if (role == Role.MENTOR) {
            studentPage = studentRepository.findStudentsByMentorId(userId, pageable);
        } else {
            throw new IllegalArgumentException("Khong co quyen truy cap");
        }

        return studentPage.map(studentMapper::toStudentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();

        Student student;
        if (role == Role.ADMIN) {
            student = studentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay sinh vien voi id: " + id));
        } else if (role == Role.MENTOR) {
            student = studentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay sinh vien voi id: " + id));
            // MENTOR chỉ xem sinh viên được phân công
            boolean isAssigned = studentRepository.isStudentAssignedToMentor(id, userId);
            if (!isAssigned) {
                throw new AccessDeniedException("Ban khong co quyen xem thong tin sinh vien nay");
            }
        } else if (role == Role.STUDENT) {
            // STUDENT chỉ xem được thông tin của chính mình
            if (!id.equals(userId)) {
                throw new AccessDeniedException("Ban chi co the xem thong tin cua chinh minh");
            }
            student = studentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay sinh vien voi id: " + id));
        } else {
            throw new AccessDeniedException("Khong co quyen truy cap");
        }
        return studentMapper.toStudentResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + request.getUserId()));

        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Nguoi dung khong phai la sinh vien");
        }

        if (studentRepository.existsById(request.getUserId())) {
            throw new ResourceConflictException("Nguoi dung da duoc tao thong tin sinh vien");
        }

        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new ResourceConflictException("Ma sinh vien da ton tai");
        }

        Student student = Student.builder()
                .user(user)
                .studentCode(request.getStudentCode())
                .major(request.getMajor())
                .clazz(request.getClazz())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        student = studentRepository.save(student);
        log.info("Student profile created: userId={}, studentCode={}", student.getId(), student.getStudentCode());
        return studentMapper.toStudentResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Integer studentId, StudentUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();

        // STUDENT chỉ được tự cập nhật thông tin của mình
        if (role == Role.STUDENT && !studentId.equals(userId)) {
            throw new AccessDeniedException("Ban chi co the cap nhat thong tin cua chinh minh");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay sinh vien voi id: " + studentId));

        if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode(), studentId)) {
            throw new ResourceConflictException("Ma sinh vien da ton tai");
        }

        student.setStudentCode(request.getStudentCode());
        student.setMajor(request.getMajor());
        student.setClazz(request.getClazz());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());

        student = studentRepository.save(student);
        log.info("Student profile updated: id={}, studentCode={}", studentId, student.getStudentCode());
        return studentMapper.toStudentResponse(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Integer id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay sinh vien voi id: " + id));

        if (internshipAssignmentRepository.existsByStudentId(id)) {
            throw new ResourceConflictException("Khong the xoa sinh vien dang co lich su phan cong thuc tap");
        }

        User user = student.getUser();
        try {
            studentRepository.delete(student);
            studentRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Student profile deleted: id={}, user deactivated", id);
    }
}

