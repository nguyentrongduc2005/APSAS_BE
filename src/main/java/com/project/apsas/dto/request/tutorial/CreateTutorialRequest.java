package com.project.apsas.dto.request.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class CreateTutorialRequest {
    private String title;
    private String summary;
}
