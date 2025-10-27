package com.project.apsas.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignments")
public class assignments {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutorial_id")
    private Long tutorialId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "order_no")
    private Integer orderNo;

    @Column(name = "statement_md", columnDefinition = "MEDIUMTEXT")
    private String statementMd;

    @Column(name = "max_score", precision = 6, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "attempts_limit")
    private Integer attemptsLimit;

    @Column(length = 80)
    private String proficiency;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    //  truy vấn tutorial đi kèm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutorial_id", insertable = false, updatable = false)
    private Tutorial tutorial;

    // truy vấn kỹ năng liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private Set<courses_assignments> courseLinks = new HashSet<>();
    
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private Set<assignment_evaluation_maps> evaluationLinks = new HashSet<>();

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private Set<Submission> submissions = new HashSet<>();
    
}
