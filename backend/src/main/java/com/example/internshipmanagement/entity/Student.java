package com.example.internshipmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "students")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Student {

    @Id
    @Column(name = "student_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id")
    private User user;

    @Column(name = "student_code", unique = true, nullable = false, length = 20)
    private String studentCode;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "class", length = 50)
    private String clazz;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 255)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
