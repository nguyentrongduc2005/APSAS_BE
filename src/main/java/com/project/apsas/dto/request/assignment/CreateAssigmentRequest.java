package com.project.apsas.dto.request.assignment;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateAssigmentRequest {
    private Long skillId;
    private String title;
    private String statementMd;
    private BigDecimal maxScore;
    private int proficiency;
    private Integer orderNo;
    private Integer attemptsLimit;

    // Thông tin cho AssignmentEvaluation (CON)
    // Đây là mấu chốt: một danh sách lồng bên trong
    private List<CreateEvaluationRequest> evaluations;
}
