package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.round.RoundCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.round.RoundCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.round.RoundCriterionResponse;
import com.example.internshipmanagement.entity.AssessmentRound;
import com.example.internshipmanagement.entity.EvaluationCriterion;
import com.example.internshipmanagement.entity.RoundCriterion;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.mapper.RoundCriterionMapper;
import com.example.internshipmanagement.repository.AssessmentRoundRepository;
import com.example.internshipmanagement.repository.EvaluationCriterionRepository;
import com.example.internshipmanagement.repository.RoundCriterionRepository;
import com.example.internshipmanagement.service.RoundCriterionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoundCriterionServiceImpl implements RoundCriterionService {

    private final RoundCriterionRepository roundCriterionRepository;
    private final AssessmentRoundRepository assessmentRoundRepository;
    private final EvaluationCriterionRepository evaluationCriterionRepository;
    private final RoundCriterionMapper roundCriterionMapper;

    @Override
    public List<RoundCriterionResponse> getCriteriaByRoundId(Integer roundId) {
        List<RoundCriterion> roundCriteria = roundCriterionRepository.findByRoundId(roundId);
        return roundCriteria.stream()
                .map(roundCriterionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoundCriterionResponse getRoundCriterionById(Integer id) {
        RoundCriterion roundCriterion = roundCriterionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay tieu chi trong vong danh gia voi id: " + id));
        return roundCriterionMapper.toResponse(roundCriterion);
    }

    @Override
    @Transactional
    public RoundCriterionResponse createRoundCriterion(RoundCriterionCreateRequest request) {
        AssessmentRound round = assessmentRoundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay dot danh gia co id: " + request.getRoundId()));
        
        EvaluationCriterion criterion = evaluationCriterionRepository.findById(request.getCriterionId())
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay tieu chi co id: " + request.getCriterionId()));

        if (roundCriterionRepository.existsByRoundIdAndCriterionId(request.getRoundId(), request.getCriterionId())) {
            throw new ResourceConflictException("Tieu chi nay da ton tai trong dot danh gia");
        }

        RoundCriterion roundCriterion = RoundCriterion.builder()
                .round(round)
                .criterion(criterion)
                .weight(request.getWeight())
                .build();

        RoundCriterion saved = roundCriterionRepository.save(roundCriterion);
        log.info("Round criterion created: id={}, roundId={}, criterionId={}, weight={}",
                saved.getId(), request.getRoundId(), request.getCriterionId(), request.getWeight());
        return roundCriterionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoundCriterionResponse updateRoundCriterion(Integer id, RoundCriterionUpdateRequest request) {
        RoundCriterion roundCriterion = roundCriterionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay tieu chi trong vong danh gia voi id: " + id));

        roundCriterion.setWeight(request.getWeight());
        RoundCriterion updated = roundCriterionRepository.save(roundCriterion);
        log.info("Round criterion updated: id={}, newWeight={}", id, request.getWeight());
        return roundCriterionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoundCriterion(Integer id) {
        RoundCriterion roundCriterion = roundCriterionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khong tim thay tieu chi trong vong danh gia voi id: " + id));

        try {
            roundCriterionRepository.delete(roundCriterion);
            roundCriterionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }
        log.info("Round criterion deleted: id={}", id);
    }
}
