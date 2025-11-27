package com.project.apsas.dto.skill;

import com.project.apsas.enums.CategorySkill;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private Long id;
    private String name;
    private String description;
    private CategorySkill category;
}