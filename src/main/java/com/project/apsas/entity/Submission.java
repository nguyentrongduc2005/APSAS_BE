package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Nhiều submission có thể thuộc về 1 assignment
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assignment_id", nullable = false)
//    Assignment assignment;

    // Nhiều submission có thể thuộc về 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @Column(length = 40)
    String language;

    @Column(columnDefinition = "MEDIUMTEXT")
    String code;

    @Column(columnDefinition = "LONGTEXT")
    String reportJson;

    @Column(precision = 6, scale = 2)
    BigDecimal score;

    @Column(columnDefinition = "TEXT")
    String feedback;

    @Column
    Boolean passed;

    @Column(name = "attempt_no", columnDefinition = "INT UNSIGNED DEFAULT 1")
    Integer attemptNo;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    LocalDateTime submittedAt;
}
