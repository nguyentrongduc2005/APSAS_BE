package com.project.apsas.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResourceDTO {
    private Long id;
    private String title;
    private String tutorialTitle;
    private String skillName;
    private BigDecimal maxScore;
    private Integer attemptsLimit;
}