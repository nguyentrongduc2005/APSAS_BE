package com.project.apsas.service;

import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;

public interface CourseServices {
    CourseItemResponse create(CreateCourseRequest req);
    public PagedResponse<PublicCourseItem> getPublicCourses(int page, int limit, String search);
}
