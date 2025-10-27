package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses_contents")
@IdClass(CourseContent.PK.class)
public class CourseContent {
 @Id
    @Column(name = "courses_id", nullable = false)
    private Long courseId;

    @Id
    @Column(name = "contents_id", nullable = false)
    private Long contentId;

    // Quan hệ tới Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courses_id", insertable = false, updatable = false)
    private Course course;

    // Quan hệ tới Content
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contents_id", insertable = false, updatable = false)
    private Content content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long courseId;
        private Long contentId;
    }
}
