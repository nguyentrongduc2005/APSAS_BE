package com.project.apsas.integration.jubge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRCERequest {
    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("language_id")
    private int languageId;

    @JsonProperty("stdin")
    private String stdin;

    // THÊM TRƯỜNG NÀY:
    @JsonProperty("expected_output")
    private String expectedOutput;
}
