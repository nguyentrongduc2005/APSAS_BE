package com.project.apsas.dto.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitCodeEvent {
    private String code;
    private int languageId;
    private String configJson;
    private Long submissionId;
}
