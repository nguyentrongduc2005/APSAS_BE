package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.admin.ReviewTutorialRequest;
import com.project.apsas.dto.response.admin.TutorialManagementResponse;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.service.TutorialManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tutorials")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorialManagementController {

    TutorialManagementService tutorialManagementService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<Page<TutorialManagementResponse>> getAllTutorials(
            @RequestParam(required = false) TutorialStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ApiResponse.<Page<TutorialManagementResponse>>builder()
                .code("ok")
                .message("Get all tutorials successfully")
                .data(tutorialManagementService.getAllTutorials(status, keyword, pageable))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{tutorialId}")
    public ApiResponse<TutorialManagementResponse> getTutorialDetail(@PathVariable Long tutorialId) {
        return ApiResponse.<TutorialManagementResponse>builder()
                .code("ok")
                .message("Get tutorial detail successfully")
                .data(tutorialManagementService.getTutorialDetail(tutorialId))
                .build();
    }

    /**
     * Duyệt tutorial - chuyển trạng thái thành PUBLISHED
     * PUT /api/admin/tutorials/{tutorialId}/publish
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{tutorialId}/publish")
    public ApiResponse<TutorialManagementResponse> publishTutorial(@PathVariable Long tutorialId) {
        return ApiResponse.<TutorialManagementResponse>builder()
                .code("ok")
                .message("Tutorial đã được phát hành thành công")
                .data(tutorialManagementService.publishTutorial(tutorialId))
                .build();
    }
}
