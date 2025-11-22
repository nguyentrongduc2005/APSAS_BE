package com.project.apsas.dto.response.tutorial;

import com.project.apsas.dto.mapping.TestCase;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DetailAssignmentResponse {
    private Long id;
    private String title;
    private int orderNo;
    private String statementHtml;
    private BigDecimal maxScore;
    private Integer attemptsLimit;
    private int proficiency;
    private List<TestCase> testCases;
    private LocalDateTime createdDate;
}
