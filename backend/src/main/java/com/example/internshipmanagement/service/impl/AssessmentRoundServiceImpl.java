package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.round.AssessmentRoundCreateRequest;
import com.example.internshipmanagement.dto.request.round.AssessmentRoundUpdateRequest;
import com.example.internshipmanagement.dto.request.round.RoundCriterionRequest;
import com.example.internshipmanagement.dto.response.round.AssessmentRoundResponse;
import com.example.internshipmanagement.entity.AssessmentRound;
import com.example.internshipmanagement.entity.InternshipPhase;
import com.example.internshipmanagement.entity.EvaluationCriterion;
import com.example.internshipmanagement.entity.RoundCriterion;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.exception.ResourceConflictException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.internshipmanagement.mapper.AssessmentRoundMapper;
import com.example.internshipmanagement.repository.AssessmentRoundRepository;
import com.example.internshipmanagement.repository.IEvaluationCriterionRepository;
import com.example.internshipmanagement.repository.IInternshipPhaseRepository;
import com.example.internshipmanagement.repository.RoundCriterionRepository;
import com.example.internshipmanagement.service.AssessmentRoundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentRoundServiceImpl implements AssessmentRoundService {
    private final AssessmentRoundRepository assessmentRoundRepository;
    private final IInternshipPhaseRepository internshipPhaseRepository;
    private final AssessmentRoundMapper assessmentRoundMapper;
    private final IEvaluationCriterionRepository evaluationCriterionRepository;
    private final RoundCriterionRepository roundCriterionRepository;
    @Override
    public Page<AssessmentRoundResponse> getAssessmentRounds(Integer phase_id, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();

        Page<AssessmentRound> assessmentRoundPage;

        if (role == Role.ADMIN) {
            if (phase_id != null) {
                internshipPhaseRepository.findById(phase_id)
                        .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot thuc tap co id: " + phase_id));
                assessmentRoundPage = assessmentRoundRepository.findByPhaseId(phase_id, pageable);
            } else {
                assessmentRoundPage = assessmentRoundRepository.findAll(pageable);
            }
        } else {
            if (phase_id != null) {
                internshipPhaseRepository.findById(phase_id)
                        .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot thuc tap co id: " + phase_id));
                assessmentRoundPage = assessmentRoundRepository.findByPhaseIdAndIsActive(phase_id, true, pageable);
            } else {
                assessmentRoundPage = assessmentRoundRepository.findByIsActive(true, pageable);
            }
        }

        return assessmentRoundPage.map(assessmentRoundMapper::toResponse);
    }

    @Override
    public AssessmentRoundResponse getAssessmentRoundById(Integer id) {
        AssessmentRound assessmentRound = assessmentRoundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot danh gia id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();

        if (role != Role.ADMIN && !assessmentRound.getIsActive()) {
            throw new ResourceNotFoundException("khong tim thay dot danh gia id: " + id);
        }

        return assessmentRoundMapper.toResponse(assessmentRound);
    }

    @Override
    @Transactional
    public AssessmentRoundResponse createAssessmentRound(AssessmentRoundCreateRequest request) {
        InternshipPhase phase = internshipPhaseRepository.findById(request.getPhaseId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot thuc tap co id: " + request.getPhaseId()));

        AssessmentRound assessmentRound = assessmentRoundMapper.toEntity(request);
        assessmentRound.setPhase(phase);
        
        AssessmentRound savedRound = assessmentRoundRepository.save(assessmentRound);
        java.util.List<RoundCriterion> createdCriteria = new java.util.ArrayList<>();

        for (RoundCriterionRequest criterionReq : request.getCriteria()) {
            EvaluationCriterion criterion = evaluationCriterionRepository.findById(criterionReq.getCriterionId())
                    .orElseThrow(() -> new ResourceNotFoundException("khong tim thay tieu chi co id: " + criterionReq.getCriterionId()));

            RoundCriterion roundCriterion = RoundCriterion.builder()
                    .round(savedRound)
                    .criterion(criterion)
                    .weight(criterionReq.getWeight())
                    .build();
            
            createdCriteria.add(roundCriterionRepository.save(roundCriterion));
        }

        savedRound.setRoundCriteria(createdCriteria);

        log.info("Assessment round created: id={}, phaseId={}, criteriaCount={}",
                savedRound.getId(), request.getPhaseId(), request.getCriteria().size());
        return assessmentRoundMapper.toResponse(savedRound);
    }

    @Override
    @Transactional
    public AssessmentRoundResponse updateAssessmentRound(Integer id, AssessmentRoundUpdateRequest request) {
        AssessmentRound assessmentRound = assessmentRoundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot danh gia id: " + id));

        assessmentRoundMapper.updateEntity(assessmentRound, request);

        if (request.getPhaseId() != null) {
            InternshipPhase phase = internshipPhaseRepository.findById(request.getPhaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot thuc tap co id: " + request.getPhaseId()));
            assessmentRound.setPhase(phase);
        }

        AssessmentRound updatedRound = assessmentRoundRepository.save(assessmentRound);
        log.info("Assessment round updated: id={}", id);
        return assessmentRoundMapper.toResponse(updatedRound);
    }

    @Override
    @Transactional
    public Void deleteAssessmentRound(Integer id) {
        AssessmentRound assessmentRound = assessmentRoundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot danh gia id: " + id));

        java.util.List<RoundCriterion> roundCriteria = roundCriterionRepository.findByRoundId(id);
        if (!roundCriteria.isEmpty()) {
            roundCriterionRepository.deleteAll(roundCriteria);
        }

        try {
            assessmentRoundRepository.delete(assessmentRound);
            assessmentRoundRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Không thể xóa dữ liệu này vì đang được tham chiếu hoặc sử dụng bởi các dữ liệu khác.");
        }
        log.info("Assessment round deleted: id={}, removedCriteriaCount={}", id, roundCriteria.size());
        return null;
    }
}
