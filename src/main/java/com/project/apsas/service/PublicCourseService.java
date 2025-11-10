package com.project.apsas.service;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.repository.CoursePublicRepository;
import com.project.apsas.repository.projection.PublicCourseRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCourseService {

    private final CoursePublicRepository repo;

    public PagedResponse<PublicCourseItem> list(int page, int limit, String search) {
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 60);
        int offset = (pg - 1) * lm;

        String kw = (search == null) ? "" : search.trim();
        String likeKw = "%" + kw + "%";

        List<PublicCourseRow> rows = repo.findPublicCourses(kw, likeKw, lm, offset);
        long total = repo.countPublicCourses(kw, likeKw);
        int totalPages = (int) Math.ceil(total / (double) lm);

        List<PublicCourseItem> data = rows.stream().map(r -> PublicCourseItem.builder()
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
                .visibility(r.getVisibility())
                .build()
        ).toList();

        return PagedResponse.<PublicCourseItem>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(total)
                        .totalPages(totalPages)
                        .hasNext(pg < totalPages)
                        .hasPrev(pg > 1)
                        .build())
                .build();
    }
}
