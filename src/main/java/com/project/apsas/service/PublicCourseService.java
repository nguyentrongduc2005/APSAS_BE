package com.project.apsas.service;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;

public interface PublicCourseService {
    PagedResponse<PublicCourseItem> getPublicCourses(int page, int limit, String search);
}
