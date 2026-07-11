package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.RoundCriterion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundCriterionRepository extends JpaRepository<RoundCriterion, Integer> {
    @EntityGraph(attributePaths = {"round", "criterion"})
    List<RoundCriterion> findByRoundId(Integer roundId);

    boolean existsByRoundIdAndCriterionId(Integer roundId, Integer criterionId);

    long countByRoundId(Integer roundId);

    @Query("SELECT COUNT(rc) FROM RoundCriterion rc " +
           "WHERE rc.round.phase.id = :phaseId AND rc.round.isActive = true")
    long countRequiredCriteriaByPhaseId(@Param("phaseId") Integer phaseId);
}
