package com.project.apsas.repository.projection;

import java.time.LocalDateTime;

public interface PublicCourseRow {
    Long getId();
    String getName();
    String getCode();
    Long getInstructorId();
    String getInstructorName();
    Long getStudentsCount();
    Long getLessonsCount();
    LocalDateTime getCreatedAt();
    String getVisibility();
}
