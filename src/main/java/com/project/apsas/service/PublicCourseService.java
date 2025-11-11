package com.project.apsas.service;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;

import java.util.List;


public interface PublicCourseService {



    public PagedResponse<PublicCourseItem> getPublicCourses(int page, int limit, String search);
}
