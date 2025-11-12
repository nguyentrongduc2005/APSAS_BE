package com.project.apsas.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseItemTeacherResponse {

    private Long id;
    private String name;
    private Long totalLession;
    private Long currentMember;
    private String visibility;
    private Integer limit;
    private String type;
    private String avatarUrl;

}
