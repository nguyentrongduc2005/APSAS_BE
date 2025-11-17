package com.project.apsas.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatisticsResponse {
    private Long totalContents;
    private Long pendingContents;
    private Long publishedContents;
    private Long rejectedContents;
    private Long draftContents;
}
