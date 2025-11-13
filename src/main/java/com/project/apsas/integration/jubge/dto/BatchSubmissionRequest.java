package com.project.apsas.integration.jubge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchSubmissionRequest {
    private List<SubmissionRCERequest> submissions;
}
