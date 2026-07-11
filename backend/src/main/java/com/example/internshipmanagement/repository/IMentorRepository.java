package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.Mentor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMentorRepository extends JpaRepository<Mentor, Integer>
{
    @Query("SELECT COUNT(a) > 0 FROM InternshipAssignment a " +
            "WHERE a.student.id = :studentId AND a.mentor.id = :mentorId")
    boolean isMentorAssignedToStudent(@Param("studentId") Integer studentId, @Param("mentorId") Integer mentorId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Mentor> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Mentor> findAll(Pageable pageable);
}
