package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.InternshipAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipAssignmentRepository extends JpaRepository<InternshipAssignment, Integer> {
    @Override
    @EntityGraph(attributePaths = {"student", "student.user", "mentor", "mentor.user", "phase"})
    List<InternshipAssignment> findAll();

    @Override
    @EntityGraph(attributePaths = {"student", "student.user", "mentor", "mentor.user", "phase"})
    Page<InternshipAssignment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "mentor", "mentor.user", "phase"})
    Page<InternshipAssignment> findByMentorId(Integer mentorId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "mentor", "mentor.user", "phase"})
    Page<InternshipAssignment> findByStudentId(Integer studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "mentor", "mentor.user", "phase"})
    Page<InternshipAssignment> findByPhaseId(Integer phaseId, Pageable pageable);

    boolean existsByStudentIdAndPhaseId(Integer studentId, Integer phaseId);

    boolean existsByStudentIdAndStatusIn(Integer studentId, java.util.List<com.example.internshipmanagement.entity.enums.AssignmentStatus> statuses);

    boolean existsByMentorIdAndStatusIn(Integer mentorId, java.util.List<com.example.internshipmanagement.entity.enums.AssignmentStatus> statuses);

    boolean existsByMentorId(Integer mentorId);

    boolean existsByStudentId(Integer studentId);

    @Query("SELECT a.status, COUNT(a) FROM InternshipAssignment a GROUP BY a.status")
    List<Object[]> countByStatus();
}
