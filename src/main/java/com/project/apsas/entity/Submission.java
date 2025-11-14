package com.project.apsas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.apsas.enums.StatusSubmission;
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
@EqualsAndHashCode(of = {"id"})
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(length = 40)
    private String language;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String code;
    @Enumerated(EnumType.STRING)
    private StatusSubmission status;

    @Column(columnDefinition = "TEXT")
    private String suggestion;
    @Column(name = "big_o_complexity_time",length = 40)
    private String bigOComplexityTime;
    @Column(name = "big_o_complexity_space",length = 40)
    private String bigOComplexitySpace;

    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Boolean passed;

    @Column(name = "report_json", columnDefinition = "LONGTEXT")
    private String reportJson;


    @Column(name = "attempt_no")
    private Integer attemptNo;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    @JsonIgnore
    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Feedback> feedbacks;
}
