package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IStudentRepository extends JpaRepository<Student, Integer> {
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN InternshipAssignment a ON a.student = s " +
           "WHERE a.mentor.id = :mentorId")
    List<Student> findStudentsByMentorId(@Param("mentorId") Integer mentorId);

    @EntityGraph(attributePaths = {"user"})
    @Query(value = "SELECT DISTINCT s FROM Student s " +
           "JOIN InternshipAssignment a ON a.student = s " +
           "WHERE a.mentor.id = :mentorId",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Student s " +
           "JOIN InternshipAssignment a ON a.student = s " +
           "WHERE a.mentor.id = :mentorId")
    Page<Student> findStudentsByMentorId(@Param("mentorId") Integer mentorId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Student> findAll();

    @Query("SELECT COUNT(a) > 0 FROM InternshipAssignment a " +
           "WHERE a.student.id = :studentId AND a.mentor.id = :mentorId")
    boolean isStudentAssignedToMentor(@Param("studentId") Integer studentId, @Param("mentorId") Integer mentorId);

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndIdNot(String studentCode, Integer id);
}
