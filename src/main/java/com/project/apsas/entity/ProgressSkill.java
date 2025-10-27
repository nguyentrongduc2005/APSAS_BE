package com.project.apsas.entity;

import com.project.apsas.entity.ProgressSkillId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProgressSkillId.class)
@Table(name = "progress_skills")
public class ProgressSkill {

    @Id
    @Column(name = "progress_id")
    private Long progressId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    private Integer level;

    private BigDecimal score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", insertable = false, updatable = false)
    private Progress progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;
}
