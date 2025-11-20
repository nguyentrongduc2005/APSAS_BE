package com.project.apsas.dto.response.course;

import com.project.apsas.enums.ContentStatus; // Nhớ import Enum này
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResourceDTO {
    private Long id;
    private String title;
    private String tutorialTitle;
    private ContentStatus status;

    private Integer orderNo;
}