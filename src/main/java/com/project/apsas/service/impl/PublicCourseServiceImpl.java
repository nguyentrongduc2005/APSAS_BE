package com.project.apsas.service.impl;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.CoursePublicRepository;
import com.project.apsas.repository.projection.PublicCourseRow;
import com.project.apsas.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCourseServiceImpl implements PublicCourseService {

    private final CoursePublicRepository coursePublicRepository;

    @Override
    public PagedResponse<PublicCourseItem> getPublicCourses(int page, int limit, String search) {
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 60);
        int offset = (pg - 1) * lm;

        String kw = (search == null) ? "" : search.trim();
        String likeKw = "%" + kw + "%";

        List<PublicCourseRow> rows = coursePublicRepository.findPublicCourses(kw, likeKw, lm, offset);
        long total = coursePublicRepository.countPublicCourses(kw, likeKw);

        if (pg > 1 && rows.isEmpty()) throw new AppException(ErrorCode.NOT_FOUND);

        List<PublicCourseItem> items = rows.stream().map(r -> PublicCourseItem.builder()
                .id(r.getId())
                .name(r.getName())
                .code(r.getCode())
                .instructor(PublicCourseItem.Instructor.builder()
                        .id(r.getInstructorId())
                        .name(r.getInstructorName())
                        .build())
                .studentsCount(r.getStudentsCount())
                .lessonsCount(r.getLessonsCount())
                .createdAt(r.getCreatedAt())
                // Nếu PublicCourseItem.visibility là String → convert enum → String
                .visibility(r.getVisibility())
                // Nếu DTO dùng enum CourseVisibility: dùng .visibility(r.getVisibility()) thay vì .name()
                .build()
        ).toList();

        return PagedResponse.<PublicCourseItem>builder()
                .data(items)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(total)
                        .totalPages((int) Math.ceil(total / (double) lm))
                        .hasNext(pg * (long) lm < total)
                        .hasPrev(pg > 1)
                        .build())
                .build();
    }
}
