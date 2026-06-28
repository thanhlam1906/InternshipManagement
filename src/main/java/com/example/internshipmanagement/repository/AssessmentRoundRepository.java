package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.AssessmentRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentRoundRepository extends JpaRepository<AssessmentRound, Integer> {
    Page<AssessmentRound> findByPhaseId(Integer id, Pageable pageable);
    Page<AssessmentRound> findByIsActive(Boolean isActive, Pageable pageable);
    Page<AssessmentRound> findByPhaseIdAndIsActive(Integer phaseId, Boolean isActive, Pageable pageable);
}
