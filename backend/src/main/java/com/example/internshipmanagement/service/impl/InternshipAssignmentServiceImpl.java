package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentCreateRequest;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentStatusUpdateRequest;
import com.example.internshipmanagement.dto.request.assignment.InternshipAssignmentUpdateRequest;
import com.example.internshipmanagement.dto.response.assignment.InternshipAssignmentResponse;
import com.example.internshipmanagement.entity.InternshipAssignment;
import com.example.internshipmanagement.entity.InternshipPhase;
import com.example.internshipmanagement.entity.Mentor;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.entity.enums.AssignmentStatus;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.example.internshipmanagement.mapper.InternshipAssignmentMapper;
import com.example.internshipmanagement.repository.IInternshipAssignmentRepository;
import com.example.internshipmanagement.repository.IInternshipPhaseRepository;
import com.example.internshipmanagement.repository.IMentorRepository;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.repository.IAssessmentResultRepository;
import com.example.internshipmanagement.service.InternshipAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipAssignmentServiceImpl implements InternshipAssignmentService {

    private final IInternshipAssignmentRepository internshipAssignmentRepository;
    private final IStudentRepository studentRepository;
    private final IMentorRepository mentorRepository;
    private final IInternshipPhaseRepository internshipPhaseRepository;
    private final IAssessmentResultRepository assessmentResultRepository;
    private final InternshipAssignmentMapper internshipAssignmentMapper;

    @Override
    public Page<InternshipAssignmentResponse> getAssignments(Integer phaseId, Integer studentId, Integer mentorId, Pageable pageable) {
        CustomUserDetails userDetails = CustomUserDetails.getCurrentUser();
        Role role = userDetails.getRole();
        Integer currentUserId = userDetails.getUserId();

        Page<InternshipAssignment> assignmentsPage;

        if (role == Role.ADMIN) {
            if (studentId != null) {
                assignmentsPage = internshipAssignmentRepository.findByStudentId(studentId, pageable);
            } else if (mentorId != null) {
                assignmentsPage = internshipAssignmentRepository.findByMentorId(mentorId, pageable);
            } else if (phaseId != null) {
                assignmentsPage = internshipAssignmentRepository.findByPhaseId(phaseId, pageable);
            } else {
                assignmentsPage = internshipAssignmentRepository.findAll(pageable);
            }
        } else if (role == Role.MENTOR) {
            assignmentsPage = internshipAssignmentRepository.findByMentorId(currentUserId, pageable);
        } else if (role == Role.STUDENT) {
            assignmentsPage = internshipAssignmentRepository.findByStudentId(currentUserId, pageable);
        } else {
            throw new AccessDeniedException("Khong co quyen truy cap");
        }

        return assignmentsPage.map(internshipAssignmentMapper::toResponse);
    }

    @Override
    public InternshipAssignmentResponse getAssignmentById(Integer id) {
        InternshipAssignment assignment = internshipAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay phan cong co id: " + id));

        CustomUserDetails userDetails = CustomUserDetails.getCurrentUser();
        Role role = userDetails.getRole();
        Integer currentUserId = userDetails.getUserId();

        if (role != Role.ADMIN) {
            if (role == Role.MENTOR && !assignment.getMentor().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Ban khong co quyen xem phan cong nay");
            }
            if (role == Role.STUDENT && !assignment.getStudent().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Ban khong co quyen xem phan cong nay");
            }
        }

        return internshipAssignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public InternshipAssignmentResponse createAssignment(InternshipAssignmentCreateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay sinh vien co id: " + request.getStudentId()));

        Mentor mentor = mentorRepository.findById(request.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay giang vien co id: " + request.getMentorId()));

        InternshipPhase phase = internshipPhaseRepository.findById(request.getPhaseId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay giai doan thuc tap co id: " + request.getPhaseId()));

        if (internshipAssignmentRepository.existsByStudentIdAndPhaseId(request.getStudentId(), request.getPhaseId())) {
            throw new ResourceConflictException("Sinh vien da duoc phan cong thuc tap trong giai doan nay");
        }

        InternshipAssignment assignment = InternshipAssignment.builder()
                .student(student)
                .mentor(mentor)
                .phase(phase)
                .status(AssignmentStatus.PENDING)
                .build();

        InternshipAssignment saved = internshipAssignmentRepository.save(assignment);
        log.info("Assignment created: id={}, studentId={}, mentorId={}, phaseId={}",
                saved.getId(), request.getStudentId(), request.getMentorId(), request.getPhaseId());
        return internshipAssignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InternshipAssignmentResponse updateAssignment(Integer id, InternshipAssignmentUpdateRequest request) {
        InternshipAssignment assignment = internshipAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay phan cong co id: " + id));

        Integer studentId = request.getStudentId() != null ? request.getStudentId() : assignment.getStudent().getId();
        Integer phaseId = request.getPhaseId() != null ? request.getPhaseId() : assignment.getPhase().getId();

        if (request.getStudentId() != null || request.getPhaseId() != null) {
            boolean studentChanged = request.getStudentId() != null && !assignment.getStudent().getId().equals(request.getStudentId());
            boolean phaseChanged = request.getPhaseId() != null && !assignment.getPhase().getId().equals(request.getPhaseId());
            
            if (studentChanged || phaseChanged) {
                if (internshipAssignmentRepository.existsByStudentIdAndPhaseId(studentId, phaseId)) {
                    throw new ResourceConflictException("Sinh vien da duoc phan cong thuc tap trong giai doan nay");
                }
            }
        }

        if (request.getStudentId() != null) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("khong tim thay sinh vien co id: " + request.getStudentId()));
            assignment.setStudent(student);
        }

        if (request.getMentorId() != null) {
            Mentor mentor = mentorRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("khong tim thay giang vien co id: " + request.getMentorId()));
            assignment.setMentor(mentor);
        }

        if (request.getPhaseId() != null) {
            InternshipPhase phase = internshipPhaseRepository.findById(request.getPhaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("khong tim thay giai doan thuc tap co id: " + request.getPhaseId()));
            assignment.setPhase(phase);
        }

        InternshipAssignment updated = internshipAssignmentRepository.save(assignment);
        log.info("Assignment updated: id={}", id);
        return internshipAssignmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public InternshipAssignmentResponse updateAssignmentStatus(Integer id, InternshipAssignmentStatusUpdateRequest request) {
        InternshipAssignment assignment = internshipAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay phan cong co id: " + id));

        assignment.setStatus(request.getStatus());
        InternshipAssignment updated = internshipAssignmentRepository.save(assignment);
        log.info("Assignment status updated: id={}, newStatus={}", id, request.getStatus());
        return internshipAssignmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAssignment(Integer id) {
        InternshipAssignment assignment = internshipAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay phan cong co id: " + id));

        if (assessmentResultRepository.existsByAssignmentId(id)) {
            throw new ResourceConflictException("Khong the xoa phan cong da co ket qua danh gia");
        }

        try {
            internshipAssignmentRepository.delete(assignment);
            internshipAssignmentRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }
        log.info("Assignment deleted: id={}", id);
    }
}
