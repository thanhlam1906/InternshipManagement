package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.AssessmentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAssessmentResultRepository extends JpaRepository<AssessmentResult, Integer> {
    @Override
    @EntityGraph(attributePaths = {"round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentId(Integer assignmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentStudentId(Integer studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByAssignmentMentorId(Integer mentorId, Pageable pageable);

    boolean existsByAssignmentIdAndRoundIdAndCriterionId(Integer assignmentId, Integer roundId, Integer criterionId);

    long countByAssignmentId(Integer assignmentId);

    boolean existsByAssignmentId(Integer assignmentId);

    @EntityGraph(attributePaths = {"round", "criterion", "evaluatedBy"})
    Page<AssessmentResult> findByRoundId(Integer roundId, Pageable pageable);
}
