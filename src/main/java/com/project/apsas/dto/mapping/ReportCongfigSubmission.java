package com.project.apsas.dto.mapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCongfigSubmission {
    private double averageTime;
    private double averageMemory;
    private int totalTestCases;
    private int passedTestCases;
    private List<TestCaseResult> testCases;
}
