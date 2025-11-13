package com.project.apsas.dto.mapping;

import com.project.apsas.enums.EvaluationVisibility;
import lombok.Data;

@Data
public class TestCase {
    private String in; // Dữ liệu stdin
    private String out; // Dữ liệu expected_output
    private EvaluationVisibility visibility; // Test case này ẩn hay hiện
}
