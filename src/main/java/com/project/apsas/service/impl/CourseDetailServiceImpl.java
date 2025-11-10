package com.project.apsas.service.impl;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.CourseDetailResponse;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.CourseDetailRepository;
import com.project.apsas.repository.projection.CourseDetailRow;
import com.project.apsas.service.CourseDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseDetailServiceImpl implements CourseDetailService {

    private final CourseDetailRepository repo;

    @Override
    public ApiResponse<CourseDetailResponse> getPublicDetail(Long courseId) {
        CourseDetailRow r = repo.findPublicDetail(courseId);
        if (r == null) throw new AppException(ErrorCode.NOT_FOUND);

        List<CourseDetailResponse.Lesson> topLessons = repo.findTopLessons(courseId).stream()
                .map(a -> CourseDetailResponse.Lesson.builder()
                        .id(((Number) a[0]).longValue())
                        .title((String) a[1])
                        .orderIndex(a[2] == null ? null : ((Number) a[2]).intValue())
                        .build())
                .toList();

        CourseDetailResponse data = CourseDetailResponse.builder()
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
                .topLessons(topLessons)
                .build();

        return ApiResponse.<CourseDetailResponse>builder()
                .code("0").message("SUCCESS").data(data).build();
    }
}
