package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.dto.request.result.AssessmentResultCreateRequest;
import com.example.internshipmanagement.dto.request.result.AssessmentResultUpdateRequest;
import com.example.internshipmanagement.dto.response.result.AssessmentResultResponse;
import com.example.internshipmanagement.entity.*;
import com.example.internshipmanagement.entity.enums.AssignmentStatus;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.mapper.AssessmentResultMapper;
import com.example.internshipmanagement.repository.*;
import com.example.internshipmanagement.service.AssessmentResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentResultServiceImpl implements AssessmentResultService {

    private final IAssessmentResultRepository assessmentResultRepository;
    private final IInternshipAssignmentRepository internshipAssignmentRepository;
    private final AssessmentRoundRepository assessmentRoundRepository;
    private final IEvaluationCriterionRepository evaluationCriterionRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    private final IUserRepository userRepository;
    private final AssessmentResultMapper assessmentResultMapper;

    @Override
    public Page<AssessmentResultResponse> getAssessmentResults(Integer assignmentId, Integer studentId,
            Integer mentorId, Integer roundId, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer currentUserId = userDetails.getUserId();

        Page<AssessmentResult> resultsPage;

        if (role == Role.ADMIN) {
            if (assignmentId != null) {
                resultsPage = assessmentResultRepository.findByAssignmentId(assignmentId, pageable);
            } else if (studentId != null) {
                resultsPage = assessmentResultRepository.findByAssignmentStudentId(studentId, pageable);
            } else if (mentorId != null) {
                resultsPage = assessmentResultRepository.findByAssignmentMentorId(mentorId, pageable);
            } else if (roundId != null) {
                resultsPage = assessmentResultRepository.findByRoundId(roundId, pageable);
            } else {
                resultsPage = assessmentResultRepository.findAll(pageable);
            }
        } else if (role == Role.MENTOR) {
            if (assignmentId != null) {
                InternshipAssignment assignment = internshipAssignmentRepository.findById(assignmentId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("khong tim thay phan cong co id: " + assignmentId));
                if (!assignment.getMentor().getId().equals(currentUserId)) {
                    throw new ResourceNotFoundException("khong tim thay phan cong co id: " + assignmentId);
                }
                resultsPage = assessmentResultRepository.findByAssignmentId(assignmentId, pageable);
            } else {
                resultsPage = assessmentResultRepository.findByAssignmentMentorId(currentUserId, pageable);
            }
        } else if (role == Role.STUDENT) {
            if (assignmentId != null) {
                InternshipAssignment assignment = internshipAssignmentRepository.findById(assignmentId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("khong tim thay phan cong co id: " + assignmentId));
                if (!assignment.getStudent().getId().equals(currentUserId)) {
                    throw new ResourceNotFoundException("khong tim thay phan cong co id: " + assignmentId);
                }
                resultsPage = assessmentResultRepository.findByAssignmentId(assignmentId, pageable);
            } else {
                resultsPage = assessmentResultRepository.findByAssignmentStudentId(currentUserId, pageable);
            }
        } else {
            throw new IllegalArgumentException("Khong co quyen truy cap");
        }

        return resultsPage.map(assessmentResultMapper::toResponse);
    }

    @Override
    public AssessmentResultResponse getAssessmentResultById(Integer id) {
        AssessmentResult result = assessmentResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay ket qua danh gia voi id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        Integer currentUserId = userDetails.getUserId();

        if (role == Role.MENTOR) {
            if (!result.getAssignment().getMentor().getId().equals(currentUserId)) {
                throw new ResourceNotFoundException("khong tim thay ket qua danh gia voi id: " + id);
            }
        } else if (role == Role.STUDENT) {
            if (!result.getAssignment().getStudent().getId().equals(currentUserId)) {
                throw new ResourceNotFoundException("khong tim thay ket qua danh gia voi id: " + id);
            }
        }

        return assessmentResultMapper.toResponse(result);
    }

    @Override
    @Transactional
    public AssessmentResultResponse createAssessmentResult(AssessmentResultCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer currentUserId = userDetails.getUserId();

        InternshipAssignment assignment = internshipAssignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "khong tim thay phan cong co id: " + request.getAssignmentId()));

        if (!assignment.getMentor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Ban khong phai mentor duoc phan cong cho sinh vien nay");
        }

        AssessmentRound round = assessmentRoundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "khong tim thay vong danh gia co id: " + request.getRoundId()));

        EvaluationCriterion criterion = evaluationCriterionRepository.findById(request.getCriterionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "khong tim thay tieu chi co id: " + request.getCriterionId()));

        // Verify that the criterion is linked to the round
        if (!roundCriterionRepository.existsByRoundIdAndCriterionId(request.getRoundId(), request.getCriterionId())) {
            throw new IllegalArgumentException("Tieu chi nay khong thuoc vong danh gia da chon");
        }

        // Verify that score does not exceed maxScore
        if (request.getScore().compareTo(criterion.getMaxScore()) > 0) {
            throw new IllegalArgumentException(
                    "Diem so khong duoc vuot qua diem so toi da cua tieu chi (" + criterion.getMaxScore() + ")");
        }

        // Check for duplicate
        if (assessmentResultRepository.existsByAssignmentIdAndRoundIdAndCriterionId(request.getAssignmentId(),
                request.getRoundId(), request.getCriterionId())) {
            throw new ResourceConflictException("Ket qua danh gia cho tieu chi nay trong vong danh gia nay da ton tai");
        }

        User mentorUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "khong tim thay thong tin nguoi dung voi id: " + currentUserId));

        AssessmentResult result = AssessmentResult.builder()
                .assignment(assignment)
                .round(round)
                .criterion(criterion)
                .score(request.getScore())
                .comments(request.getComments())
                .evaluatedBy(mentorUser)
                .evaluationDate(LocalDateTime.now())
                .build();

        AssessmentResult saved = assessmentResultRepository.save(result);
        log.info(
                "Assessment result created: id={}, assignmentId={}, roundId={}, criterionId={}, score={}, evaluatedBy={}",
                saved.getId(), request.getAssignmentId(), request.getRoundId(),
                request.getCriterionId(), request.getScore(), currentUserId);

        long requiredCount = roundCriterionRepository.countByRoundId(round.getId());
        long submittedCount = assessmentResultRepository.countByAssignmentIdAndRoundId(assignment.getId(), round.getId());

        if (requiredCount > 0 && submittedCount == requiredCount) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
            internshipAssignmentRepository.save(assignment);
            log.info("Assignment completed automatically: id={}, roundId={}", assignment.getId(), round.getId());
        } else if (submittedCount > 0 && submittedCount < requiredCount) {
            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
            internshipAssignmentRepository.save(assignment);
            log.info("Assignment marked IN_PROGRESS automatically: id={}, roundId={}", assignment.getId(), round.getId());
        }

        return assessmentResultMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AssessmentResultResponse updateAssessmentResult(Integer id, AssessmentResultUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer currentUserId = userDetails.getUserId();

        AssessmentResult result = assessmentResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay ket qua danh gia voi id: " + id));

        if (!result.getEvaluatedBy().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("Ban chi co the cap nhat ket qua danh gia do chinh ban tao");
        }

        if (request.getScore().compareTo(result.getCriterion().getMaxScore()) > 0) {
            throw new IllegalArgumentException("Diem so khong duoc vuot qua diem so toi da cua tieu chi ("
                    + result.getCriterion().getMaxScore() + ")");
        }

        result.setScore(request.getScore());
        result.setComments(request.getComments());
        result.setEvaluationDate(LocalDateTime.now());

        AssessmentResult updated = assessmentResultRepository.save(result);
        log.info("Assessment result updated: id={}, newScore={}", id, request.getScore());
        return assessmentResultMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAssessmentResult(Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer currentUserId = userDetails.getUserId();
        Role role = userDetails.getRole();

        AssessmentResult result = assessmentResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay ket qua danh gia voi id: " + id));

        if (role != Role.ADMIN && !result.getEvaluatedBy().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("Ban chi co the xoa ket qua danh gia do chinh ban tao");
        }

        InternshipAssignment assignment = result.getAssignment();
        try {
            assessmentResultRepository.delete(result);
            assessmentResultRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Không thể xóa dữ liệu này vì đang được tham chiếu hoặc sử dụng bởi các dữ liệu khác.");
        }
        log.info("Assessment result deleted: id={}, deletedBy={}", id, currentUserId);

        Integer roundId = result.getRound().getId();
        long requiredCount = roundCriterionRepository.countByRoundId(roundId);
        long submittedCount = assessmentResultRepository.countByAssignmentIdAndRoundId(assignment.getId(), roundId);

        if (submittedCount == 0) {
            assignment.setStatus(AssignmentStatus.PENDING);
        } else if (requiredCount > 0 && submittedCount < requiredCount && assignment.getStatus() == AssignmentStatus.COMPLETED) {
            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        }
        internshipAssignmentRepository.save(assignment);
    }
}
