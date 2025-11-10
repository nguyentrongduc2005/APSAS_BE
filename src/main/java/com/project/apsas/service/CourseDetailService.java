package com.project.apsas.service;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.CourseDetailResponse;

public interface CourseDetailService {
    ApiResponse<CourseDetailResponse> getPublicDetail(Long courseId);
}
