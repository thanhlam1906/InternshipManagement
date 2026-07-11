package com.example.internshipmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "assessment_results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "round_id", "criterion_id"})
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private InternshipAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private AssessmentRound round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private EvaluationCriterion criterion;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by", nullable = false)
    private User evaluatedBy;

    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssessmentResult that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
