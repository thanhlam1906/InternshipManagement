package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.AssessmentRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRoundRepository extends JpaRepository<AssessmentRound, Integer> {
    @Override
    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    List<AssessmentRound> findAll();

    @Override
    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    Page<AssessmentRound> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    List<AssessmentRound> findByPhaseId(Integer id);

    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    Page<AssessmentRound> findByPhaseId(Integer id, Pageable pageable);

    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    Page<AssessmentRound> findByIsActive(Boolean isActive, Pageable pageable);

    @EntityGraph(attributePaths = {"phase", "roundCriteria", "roundCriteria.criterion"})
    Page<AssessmentRound> findByPhaseIdAndIsActive(Integer phaseId, Boolean isActive, Pageable pageable);
}
