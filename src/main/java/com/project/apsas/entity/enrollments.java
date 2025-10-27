package com.project.apsas.entity;

import com.project.apsas.enums.EnrollmentRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "enrollments")
public class enrollments {
 @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EnrollmentRole role;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    // Quan hệ tới User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Quan hệ tới Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private courses course;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long userId;
        private Long courseId;
}
}


