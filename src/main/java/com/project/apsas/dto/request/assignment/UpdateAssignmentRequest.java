package com.project.apsas.dto.request.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class UpdateAssignmentRequest {
    private Long skillId;
    private String title;
    private String statementMd;
    private BigDecimal maxScore;
    private int proficiency;
    private Integer orderNo;
    private Integer attemptsLimit;

    // Danh sách evaluations (con)
    private List<UpdateEvaluationRequest> evaluations;
}
