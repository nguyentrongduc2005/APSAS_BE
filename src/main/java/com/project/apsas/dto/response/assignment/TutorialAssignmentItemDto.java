package com.project.apsas.dto.response.assignment;

import com.project.apsas.dto.response.tutorial.TutorialItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TutorialAssignmentItemDto implements TutorialItemDto {
    private Long id;
    private String title;
    private BigDecimal maxScore;
    private String skillName;
    private int proficiency;
    private Integer attemptsLimit;
    private Integer orderNo;
    @Override
    public String getItemType() {
        return "ASSIGNMENT";
    }
}
