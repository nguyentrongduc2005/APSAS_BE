package com.project.apsas.dto.request.assignment;

import lombok.Getter;

@Getter
public class CreateEvaluationRequest {
    private String name;
    private String type;
    private String configJson;
}
