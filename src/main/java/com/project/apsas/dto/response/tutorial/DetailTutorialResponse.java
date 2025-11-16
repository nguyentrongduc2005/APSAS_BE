package com.project.apsas.dto.response.tutorial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailTutorialResponse {
    private Long id;
    private String title;
    private String summary;
    private List<TutorialItemDto> items;
}
