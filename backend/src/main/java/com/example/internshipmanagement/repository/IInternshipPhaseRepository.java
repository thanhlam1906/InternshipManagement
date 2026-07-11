package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.InternshipPhase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IInternshipPhaseRepository extends JpaRepository<InternshipPhase, Integer> {
    boolean existsByPhaseName(String phaseName);
    boolean existsByPhaseNameAndIdNot(String phaseName, Integer id);
}
