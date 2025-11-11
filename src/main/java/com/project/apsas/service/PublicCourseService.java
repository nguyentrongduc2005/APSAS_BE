package com.project.apsas.service;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.repository.CoursePublicRepository;
import com.project.apsas.repository.projection.PublicCourseRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


public interface PublicCourseService {



    public PagedResponse<PublicCourseItem> list(int page, int limit, String search);
}
