package com.project.apsas.dto.response.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailDTO {
    private Long id;
    private String title;
    private String statementMd;
    private BigDecimal maxScore;
    private Integer attemptsLimit;
    private Integer proficiency;
    private Integer orderNo;
    private LocalDateTime openAt;
    private LocalDateTime dueAt;
    private String skillName;
    private String tutorialTitle;
}