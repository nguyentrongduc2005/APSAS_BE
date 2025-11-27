package com.project.apsas.dto.response.submission;

import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.mapping.TestCaseResult;
import com.project.apsas.entity.Feedback;
import com.project.apsas.enums.StatusSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionDetailResponse {
    private Long id;
    private String title;
    private String statementMd;
    private String language;
    private String code;
    private StatusSubmission status;
    private String suggestion;
    private String bigOComplexityTime;
    private String bigOComplexitySpace;
    private BigDecimal score;
    private String feedback;
    private Boolean passed;
    private Integer attemptNo;
    private Set<Feedback> feedbackTeachers;
    private List<TestCaseResult> testCases;
}
