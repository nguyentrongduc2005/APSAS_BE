    package com.project.apsas.entity;


    import jakarta.persistence.*;
    import lombok.*;
    import lombok.experimental.FieldDefaults;

    import java.math.BigDecimal;

    @Entity
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Table(name = "progress_skills")
    public class ProgressSkill {
        @EmbeddedId
        ProgressSkillId id = new ProgressSkillId();

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("progressId") // map phần progressId trong PK
        @JoinColumn(name = "progress_id")
        Progress progress;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("skillId") // map phần progressId trong PK
        @JoinColumn(name = "skill_id")
        Skill skill;

        int level;

        @Column(precision = 6, scale = 2)
        BigDecimal score;

    }
