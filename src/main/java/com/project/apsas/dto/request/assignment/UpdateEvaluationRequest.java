package com.project.apsas.dto.request.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateEvaluationRequest {
    private Long id;

    // Các trường của 'con'
    private String name;
    private String type; // Giả sử bạn có enum EvaluationType
    private String configJson;
}
