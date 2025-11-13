package com.project.apsas.dto.mapping;

import com.project.apsas.enums.EvaluationVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestCase {
    private String in; // Dữ liệu stdin
    private String out; // Dữ liệu expected_output
    private EvaluationVisibility visibility; // Test case này ẩn hay hiện
}
