package com.project.apsas.service;

import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;

public interface CourseServices {
    CourseItemResponse create(CreateCourseRequest req);
}
