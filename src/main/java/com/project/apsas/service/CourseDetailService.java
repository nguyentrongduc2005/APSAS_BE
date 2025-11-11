package com.project.apsas.service;

import com.project.apsas.dto.response.CourseDetailResponse;

public interface CourseDetailService {
    CourseDetailResponse getPublicDetail(Long courseId);
}
