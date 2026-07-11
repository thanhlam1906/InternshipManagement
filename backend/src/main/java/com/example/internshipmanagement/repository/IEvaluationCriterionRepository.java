package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.EvaluationCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEvaluationCriterionRepository extends JpaRepository<EvaluationCriterion, Integer> {
    boolean existsByCriterionName(String criterionName);
    boolean existsByCriterionNameAndIdNot(String criterionName, Integer id);
}
