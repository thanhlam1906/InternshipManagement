package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.InternshipPhase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IInternshipPhaseRepository extends JpaRepository<InternshipPhase, Integer> {
    @Override
    @EntityGraph(attributePaths = {})
    List<InternshipPhase> findAll();

    boolean existsByPhaseName(String phaseName);
    boolean existsByPhaseNameAndIdNot(String phaseName, Integer id);
}
