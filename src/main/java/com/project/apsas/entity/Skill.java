package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 160, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String category;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    private Set<assignments> assignments;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    private Set<ProgressSkill> progressSkills;
}
