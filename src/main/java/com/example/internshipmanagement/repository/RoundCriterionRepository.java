package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.RoundCriterion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundCriterionRepository extends JpaRepository<RoundCriterion, Integer> {
    @EntityGraph(attributePaths = {"criterion"})
    List<RoundCriterion> findByRoundId(Integer roundId);

    boolean existsByRoundIdAndCriterionId(Integer roundId, Integer criterionId);
}
