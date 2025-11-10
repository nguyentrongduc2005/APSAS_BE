package com.project.apsas.service;

import com.project.apsas.dto.response.CourseDetailResponse;
import com.project.apsas.repository.CourseDetailRepository;
import com.project.apsas.repository.projection.CourseDetailRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseDetailService {
    private final CourseDetailRepository repo;

    public CourseDetailResponse getPublicDetail(Long courseId) {
        CourseDetailRow r = repo.findPublicDetail(courseId);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found or not public");
        }

        // map lessons (optional)
        List<CourseDetailResponse.Lesson> top = repo.findTopLessons(courseId).stream()
                .map(arr -> CourseDetailResponse.Lesson.builder()
                        .id(((Number)arr[0]).longValue())
                        .title((String) arr[1])
                        .orderIndex(arr[2] == null ? null : ((Number)arr[2]).intValue())
                        .build()
                ).toList();

        return CourseDetailResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .code(r.getCode())
                .visibility(r.getVisibility())
                .createdAt(r.getCreatedAt())
                .instructor(CourseDetailResponse.Instructor.builder()
                        .id(r.getInstructorId())
                        .name(r.getInstructorName())
                        .email(r.getInstructorEmail())
                        .avatar(r.getInstructorAvatar())
                        .build())
                .studentsCount(r.getStudentsCount())
                .lessonsCount(r.getLessonsCount())
                .topLessons(top)
                .build();
    }
}
