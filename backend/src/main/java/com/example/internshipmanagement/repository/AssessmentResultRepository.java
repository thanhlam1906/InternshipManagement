package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.AssessmentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Integer> {
    @Override
    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    List<AssessmentResult> findAll();

    @Override
    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentId(Integer assignmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentStudentId(Integer studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentMentorId(Integer mentorId, Pageable pageable);

    boolean existsByAssignmentIdAndRoundIdAndCriterionId(Integer assignmentId, Integer roundId, Integer criterionId);

    long countByAssignmentId(Integer assignmentId);

    long countByAssignmentIdAndRoundId(Integer assignmentId, Integer roundId);

    boolean existsByAssignmentId(Integer assignmentId);

    @EntityGraph(attributePaths = {"assignment", "round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByRoundId(Integer roundId, Pageable pageable);
}
