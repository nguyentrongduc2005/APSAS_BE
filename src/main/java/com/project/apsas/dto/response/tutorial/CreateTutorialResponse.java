package com.project.apsas.dto.response.tutorial;

import com.project.apsas.enums.TutorialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;

@Setter
@Builder
@AllArgsConstructor
public class CreateTutorialResponse {
    private Long id;
    private String title;
    private String summary;
    private TutorialStatus status;
    private Long createdBy;

}
