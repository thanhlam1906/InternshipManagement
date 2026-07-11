package com.example.internshipmanagement.entity;

import com.example.internshipmanagement.entity.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "internship_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "phase_id"})
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InternshipAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private InternshipPhase phase;

    @CreationTimestamp
    @Column(name = "assigned_date", nullable = false, updatable = false)
    private LocalDateTime assignedDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InternshipAssignment that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
