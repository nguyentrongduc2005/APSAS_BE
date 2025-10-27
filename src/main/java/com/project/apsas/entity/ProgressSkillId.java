package com.project.apsas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressSkillId implements Serializable {
    @Column(name = "progress_id")
    Long progressId;

    @Column(name = "skill_id")
    Long skillId;
}
