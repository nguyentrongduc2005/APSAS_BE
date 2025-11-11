package com.project.apsas.repository.projection;

import java.time.LocalDateTime;

public interface CourseDetailRow {
    Long getId();
    String getName();
    String getCode();
    String getVisibility();
    LocalDateTime getCreatedAt();
    Long getInstructorId();
    String getInstructorName();
    String getInstructorEmail();
    String getInstructorAvatar();   // null nếu DB chưa có cột
    Long getStudentsCount();
    Long getLessonsCount();
}
