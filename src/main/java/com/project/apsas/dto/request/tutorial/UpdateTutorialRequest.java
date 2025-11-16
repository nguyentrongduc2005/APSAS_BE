package com.project.apsas.dto.request.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateTutorialRequest {
    private String title;
    private String summary;
}
