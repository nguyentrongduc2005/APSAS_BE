package com.project.apsas.dto.response.assignment;

import com.project.apsas.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@AllArgsConstructor
@Getter
@Builder
public class CreateAssignmentResponse {
    private Long id;
    private Long tutorialId;
    private SkillDto skill;
    private String title;
    private String statementHtml;
    private Integer orderNo;
    private int maxScore;
    private Integer attemptsLimit;
    private List<TestCaseConfig> testCaseConfigs;

}
