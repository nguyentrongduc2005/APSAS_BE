package com.project.apsas.integration.jubge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionRCEResponse {
    private String stdout;
    private String stderr;
    private String message;
    private String time;
    private Integer memory;
    private Status status;
    private String token;

    @JsonProperty("compile_output")
    private String compileOutput;

    @Data
    @AllArgsConstructor
    @Builder
    public static class Status {
        private int id;
        private String description; // Vd: "Accepted", "Wrong Answer", "Time Limit Exceeded"
    }

    public boolean isProcessing() {
        // ID 1 = In Queue
        // ID 2 = Processing
        return status != null && (status.getId() == 1 || status.getId() == 2);
    }
}
