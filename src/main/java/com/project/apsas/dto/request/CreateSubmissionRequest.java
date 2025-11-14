package com.project.apsas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSubmissionRequest {
    int languageId;
    Long assignmentId;
    Long courseId;
    String code;
}
