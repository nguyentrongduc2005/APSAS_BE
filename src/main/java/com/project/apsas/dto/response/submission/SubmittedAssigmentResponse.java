package com.project.apsas.dto.response.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmittedAssigmentResponse {
    private List<SubmissionItem> items;
}
