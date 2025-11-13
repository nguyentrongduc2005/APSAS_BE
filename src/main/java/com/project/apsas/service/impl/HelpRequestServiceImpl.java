package com.project.apsas.service.impl;

import com.project.apsas.dto.response.HelpRequestResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.entity.HelpRequest;
import com.project.apsas.repository.HelpRequestsRepository;
import com.project.apsas.service.HelpRequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HelpRequestServiceImpl implements HelpRequestService {
    HelpRequestsRepository helpRequestsRepository;

    @Override
    public PagedResponse<HelpRequestResponse> getHelpRequestsByCourse(Long courseId, int page, int limit) {
        // Validate page: minimum 1
        int pg = Math.max(page, 1);
        // Validate limit: minimum 1, maximum 100 items per page (optimized for server performance)
        int lm = Math.min(Math.max(limit, 1), 100);

        Pageable pageable = PageRequest.of(pg - 1, lm);
        Page<HelpRequest> results = helpRequestsRepository.findByCourseId(courseId, pageable);

        List<HelpRequestResponse> data = results.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        int totalPages = results.getTotalPages();
        boolean hasNext = results.hasNext();
        boolean hasPrev = results.hasPrevious();

        return PagedResponse.<HelpRequestResponse>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(results.getTotalElements())
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrev(hasPrev)
                        .build())
                .build();
    }

    private HelpRequestResponse mapToResponse(HelpRequest hr) {
        return HelpRequestResponse.builder()
                .id(hr.getId())
                .courseId(hr.getCourseId())
                .title(hr.getTitle())
                .body(hr.getBody())
                .createdAt(hr.getCreatedAt())
                .studentId(hr.getUserId())
                .studentName(hr.getUser() != null ? hr.getUser().getName() : null)
                .studentEmail(hr.getUser() != null ? hr.getUser().getEmail() : null)
                .build();
    }
}
