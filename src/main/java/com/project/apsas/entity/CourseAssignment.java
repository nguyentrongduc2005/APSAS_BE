package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses_assignments")
@IdClass(CourseAssignment.PK.class)
public class CourseAssignment {
@Id
    @Column(name = "courses_id", nullable = false)
    private Long courseId;

    @Id
    @Column(name = "assignments_id", nullable = false)
    private Long assignmentId;

    @Column(name = "open_at")
    private LocalDateTime openAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    // Quan hệ tới Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courses_id", insertable = false, updatable = false)
    private Course course;

    // Quan hệ tới Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignments_id", insertable = false, updatable = false)
    private Assignment assignment;

    // Composite Key Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long courseId;
        private Long assignmentId;
    }
}
