package com.project.apsas.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // <-- THÊM DÒNG NÀY
@AllArgsConstructor
public class SubmitCodeEvent {
    private String code;
    private int languageId;
    private String configJson;
    private Long submissionId;
}
