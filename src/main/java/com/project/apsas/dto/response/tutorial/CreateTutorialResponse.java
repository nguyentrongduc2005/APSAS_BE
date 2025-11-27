package com.project.apsas.dto.response.tutorial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.apsas.enums.TutorialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTutorialResponse {
    private Long id;
    private String title;
    private String summary;
    private TutorialStatus status;
    private Long createdBy;
    private Long totalMedia;
    private int lessonCount;        // số content / bài học
    private int assignmentCount;    // số assignment
    private int mediaCount;        // số khóa học đang dùng tutorial này

    // ---- Thông tin creator (để FE render avatar) ----
    private String creatorName;
    private String creatorAvatar;
}
