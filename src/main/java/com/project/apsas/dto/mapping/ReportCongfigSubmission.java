package com.project.apsas.dto.mapping;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ReportCongfigSubmission {
    private double averageTime;
    private double averageMemory;
    private int totalTestCases;
    private int passedTestCases;
    private List<TestCaseResult> testCases;
}
