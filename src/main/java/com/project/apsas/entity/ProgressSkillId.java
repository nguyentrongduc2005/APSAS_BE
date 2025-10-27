package com.project.apsas.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSkillId implements Serializable {

    private Long progressId;
    private Long skillId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgressSkillId that)) return false;
        return Objects.equals(progressId, that.progressId) &&
               Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(progressId, skillId);
    }
}
