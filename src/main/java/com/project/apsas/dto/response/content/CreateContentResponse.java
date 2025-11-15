package com.project.apsas.dto.response.content;

import com.project.apsas.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class CreateContentResponse {
    private Long id;
    private Long tutorialId;
    private String title;
    private String bodyMd;
    private String bodyHtmlCached; // HTML đã được chuyển đổi
    private Integer orderNo;
    private ContentStatus status;
    private int totalImage;
    private int totalVideo;
}
