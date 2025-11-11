package com.project.apsas.service;

import com.project.apsas.dto.response.CourseDetailResponse;

import java.util.List;


public interface CourseDetailService {


    public CourseDetailResponse getPublicDetail(Long courseId);
}
