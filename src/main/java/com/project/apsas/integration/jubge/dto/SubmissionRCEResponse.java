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
    private String time; // Thời gian chạy (vd: "0.011")
    private Integer memory; // Bộ nhớ (vd: 3600)

    @JsonProperty("compile_output")
    private String compileOutput;

    private Status status;
    @Data
    @AllArgsConstructor
    @Builder
    public static class Status {
        private int id;
        private String description; // Vd: "Accepted", "Wrong Answer", "Time Limit Exceeded"
    }
}
