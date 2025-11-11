package com.project.apsas.service;

import com.project.apsas.dto.response.CourseDetailResponse;
import com.project.apsas.repository.CourseDetailRepository;
import com.project.apsas.repository.projection.CourseDetailRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


public interface CourseDetailService {


    public CourseDetailResponse getPublicDetail(Long courseId);
}
