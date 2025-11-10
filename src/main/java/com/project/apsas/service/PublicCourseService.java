package com.project.apsas.service;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;

public interface PublicCourseService {
    ApiResponse<PagedResponse<PublicCourseItem>> getPublicCourses(int page, int limit, String search);
}
