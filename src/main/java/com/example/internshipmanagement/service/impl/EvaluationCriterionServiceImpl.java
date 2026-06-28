package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.criterion.EvaluationCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.evaluation_criterion.EvaluationCriterionResponse;
import com.example.internshipmanagement.entity.EvaluationCriterion;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.mapper.EvaluationCriterionMapper;
import com.example.internshipmanagement.repository.IEvaluationCriterionRepository;
import com.example.internshipmanagement.service.EvaluationCriterionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationCriterionServiceImpl implements EvaluationCriterionService {

    private final IEvaluationCriterionRepository repository;
    private final EvaluationCriterionMapper mapper;

    @Override
    public List<EvaluationCriterionResponse> getAllCriteria() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EvaluationCriterionResponse getCriterionById(Integer id) {
        EvaluationCriterion criterion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tieu chi danh gia voi id: " + id));
        return mapper.toResponse(criterion);
    }

    @Override
    @Transactional
    public EvaluationCriterionResponse createCriterion(EvaluationCriterionCreateRequest request) {
        if (repository.existsByCriterionName(request.getCriterionName())) {
            throw new ResourceConflictException("Ten tieu chi danh gia da ton tai");
        }
        EvaluationCriterion criterion = mapper.toEntity(request);
        criterion = repository.save(criterion);
        log.info("Evaluation criterion created: id={}, name={}", criterion.getId(), criterion.getCriterionName());
        return mapper.toResponse(criterion);
    }

    @Override
    @Transactional
    public EvaluationCriterionResponse updateCriterion(Integer id, EvaluationCriterionUpdateRequest request) {
        EvaluationCriterion criterion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tieu chi danh gia voi id: " + id));

        if (repository.existsByCriterionNameAndIdNot(request.getCriterionName(), id)) {
            throw new ResourceConflictException("Ten tieu chi danh gia da ton tai");
        }

        mapper.updateEntity(criterion, request);
        criterion = repository.save(criterion);
        log.info("Evaluation criterion updated: id={}, name={}", id, criterion.getCriterionName());
        return mapper.toResponse(criterion);
    }

    @Override
    @Transactional
    public void deleteCriterion(Integer id) {
        EvaluationCriterion criterion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tieu chi danh gia voi id: " + id));
        repository.delete(criterion);
        log.info("Evaluation criterion deleted: id={}", id);
    }
}

