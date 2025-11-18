package com.project.apsas.dto.response.admin;

import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentManagementResponse {
    private Long id;
    private String title;
    private MediaType primaryMediaType; // VIDEO or PDF
    private String authorName;
    private LocalDateTime createdAt;
    private ContentStatus status;
    private String tutorialTitle;
    private Long tutorialId;
    private String bodyMd;
    private List<MediaInfo> mediaList;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfo {
        private Long id;
        private MediaType type;
        private String url;
        private String caption;
    }
}
