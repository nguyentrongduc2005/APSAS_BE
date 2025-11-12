package com.project.apsas.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseItemStudentResponse {

    private Long id;
    private String name;
    private Long totalLession;
    private Long currentMember;
    private String visibility;
    private String type;
    private String avatarUrl;
    private Lecture lecture;
    private Long totalAssignment;
    private Long totalAssignmentCurrent;

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Lecture {
        private Long id;
        private String name;
        private String avatarUrl;
    }

}
