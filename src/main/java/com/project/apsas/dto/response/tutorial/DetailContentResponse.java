package com.project.apsas.dto.response.tutorial;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DetailContentResponse {
    private Long id;
    private String title;
    private String bodyHtml;
    private long totalMedia;
    private List<Media> mediaList;
    private LocalDateTime createdDate;
    @Data
    @Builder
    public static class Media
    {
        private Long id;
        private String url;
        private int orderNo;
    }
}
