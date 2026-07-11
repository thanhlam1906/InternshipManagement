package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.phase.InternshipPhaseCreateRequest;
import com.example.internshipmanagement.dto.request.phase.InternshipPhaseUpdateRequest;
import com.example.internshipmanagement.dto.response.phase.InternshipPhaseResponse;
import com.example.internshipmanagement.entity.InternshipPhase;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import com.example.internshipmanagement.mapper.InternshipPhaseMapper;
import com.example.internshipmanagement.repository.IInternshipPhaseRepository;
import com.example.internshipmanagement.service.InternshipPhaseService;
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
public class InternshipPhaseServiceImpl implements InternshipPhaseService {
    private final IInternshipPhaseRepository internshipPhaseRepository;
    private final InternshipPhaseMapper internshipPhaseMapper;

    @Override
    public Page<InternshipPhaseResponse> getAllIntershipPhase(Pageable pageable) {
        Page<InternshipPhase> internshipPhasesPage = internshipPhaseRepository.findAll(pageable);
        return internshipPhasesPage.map(internshipPhaseMapper::toInternshipPhaseResponse);
    }

    @Override
    public InternshipPhaseResponse getInternshipPhaseById(Integer id) {
        InternshipPhase internshipPhase = internshipPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot thuc tap voi id: " + id));
        return internshipPhaseMapper.toInternshipPhaseResponse(internshipPhase);
    }

    @Override
    @Transactional
    public InternshipPhaseResponse createInternshipPhase(InternshipPhaseCreateRequest request) {
        if (internshipPhaseRepository.existsByPhaseName(request.getPhaseName())) {
            throw new ResourceConflictException("Ten dot thuc tap da ton tai");
        }
        InternshipPhase internshipPhase = internshipPhaseMapper.toInternshipPhase(request);
        internshipPhase = internshipPhaseRepository.save(internshipPhase);
        log.info("Internship phase created: id={}, name={}", internshipPhase.getId(), internshipPhase.getPhaseName());
        return internshipPhaseMapper.toInternshipPhaseResponse(internshipPhase);
    }

    @Override
    @Transactional
    public InternshipPhaseResponse updateInternshipPhase(Integer id, InternshipPhaseUpdateRequest request) {
        InternshipPhase internshipPhase = internshipPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot thuc tap voi id: " + id));
        if (internshipPhaseRepository.existsByPhaseNameAndIdNot(request.getPhaseName(), id)) {
            throw new ResourceConflictException("Ten dot thuc tap da ton tai");
        }
        internshipPhaseMapper.updateInternshipPhase(internshipPhase, request);
        internshipPhase = internshipPhaseRepository.save(internshipPhase);
        log.info("Internship phase updated: id={}, name={}", id, internshipPhase.getPhaseName());
        return internshipPhaseMapper.toInternshipPhaseResponse(internshipPhase);
    }

    @Override
    @Transactional
    public void deleteInternshipPhase(Integer id) {
        InternshipPhase internshipPhase = internshipPhaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot thuc tap voi id: " + id));
        try {
            internshipPhaseRepository.delete(internshipPhase);
            internshipPhaseRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }
        log.info("Internship phase deleted: id={}", id);
    }
}

