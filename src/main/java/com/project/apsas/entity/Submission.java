package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 40)
    private String language;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String code;

    @Column(name = "report_json", columnDefinition = "LONGTEXT")
    private String reportJson;

    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Boolean passed;

    @Column(name = "attempt_no")
    private Integer attemptNo;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private assignments assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<feedback> feedbacks;
}
