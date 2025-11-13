package com.project.apsas.dto.mapping;

import com.project.apsas.enums.EvaluationVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {
    private String status; // Vd: "Accepted", "Wrong Answer"
    private double time;
    private int memory;
    private EvaluationVisibility visibility;
    private String stdin;
    private String stdout;
    private String expectedOutput;
}
