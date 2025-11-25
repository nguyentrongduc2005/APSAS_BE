package com.project.apsas.dto.response.tutorial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.apsas.enums.TutorialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchTutorialResponse {
    private Long id;
    private String title;
    private String summary;
    private TutorialStatus status;


    private int lessonCount;        // số content / bài học
    private int assignmentCount;    // số assignment// số khóa học đang dùng tutorial này
    private int mediaCount;
    private int courseCount;

    // ---- Thông tin creator (để FE render avatar) ----
    private String creatorName;
    private String creatorAvatar;
}
