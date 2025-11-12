package com.project.apsas.service;

import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;
import com.project.apsas.dto.response.CourseRegisResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseServices {
    CourseItemResponse create(CreateCourseRequest req);
    public Page<PublicCourseItem> getPublicCourses(Pageable pageable, String search);
    public CourseRegisResponse getCourseRegistrationDetails(Long courseId);
}
