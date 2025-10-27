package com.project.apsas.entity;

import com.project.apsas.enums.EvaluationVisibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignment_evaluations")
public class assignment_evaluations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 160, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private EvaluationVisibility visibility;


    @Column(length = 80, nullable = false)
    private String type;

    @Column(name = "config_json", columnDefinition = "LONGTEXT")
    private String configJson;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "evaluation", fetch = FetchType.LAZY)
    private Set<assignment_evaluation_maps> assignmentLinks = new HashSet<>();
}
