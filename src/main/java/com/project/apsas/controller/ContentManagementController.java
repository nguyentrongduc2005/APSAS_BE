package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.admin.ReviewContentRequest;
import com.project.apsas.dto.response.admin.ContentManagementResponse;
import com.project.apsas.dto.response.admin.ContentStatisticsResponse;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import com.project.apsas.service.ContentManagementService;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/contents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class ContentManagementController {

    ContentManagementService contentManagementService;

    /**
     * Lấy danh sách content với phân trang và filter
     * GET /api/admin/contents?page=0&size=10&sort=createdAt,desc&search=react&status=PENDING&mediaType=VIDEO
     */
    @GetMapping
    public ApiResponse<Page<ContentManagementResponse>> getAllContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) MediaType mediaType
    ) {
        // Handle sort safely
        Sort sortObj;
        if (sort != null && sort.length >= 2) {
            sortObj = Sort.by(
                    sort[1].equalsIgnoreCase("asc") ? Sort.Order.asc(sort[0]) : Sort.Order.desc(sort[0])
            );
        } else if (sort != null && sort.length == 1) {
            sortObj = Sort.by(Sort.Order.desc(sort[0]));
        } else {
            sortObj = Sort.by(Sort.Order.desc("createdAt"));
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<ContentManagementResponse> contents = contentManagementService.getAllContents(
                pageable, search, status, mediaType
        );

        return ApiResponse.<Page<ContentManagementResponse>>builder()
                .code("ok")
                .message("Lấy danh sách nội dung thành công")
                .data(contents)
                .build();
    }

    /**
     * Lấy chi tiết content
     * GET /api/admin/contents/{contentId}
     */
    @GetMapping("/{contentId}")
    public ApiResponse<ContentManagementResponse> getContentById(@PathVariable Long contentId) {
        ContentManagementResponse content = contentManagementService.getContentById(contentId);
        
        return ApiResponse.<ContentManagementResponse>builder()
                .code("ok")
                .message("Lấy thông tin nội dung thành công")
                .data(content)
                .build();
    }

    /**
     * Duyệt hoặc từ chối content
     * PUT /api/admin/contents/{contentId}/review
     */
    @PutMapping("/{contentId}/review")
    public ApiResponse<ContentManagementResponse> reviewContent(
            @PathVariable Long contentId,
            @Valid @RequestBody ReviewContentRequest request
    ) {
        ContentManagementResponse content = contentManagementService.reviewContent(contentId, request);
        
        return ApiResponse.<ContentManagementResponse>builder()
                .code("ok")
                .message("Xét duyệt nội dung thành công")
                .data(content)
                .build();
    }

    /**
     * Xóa content
     * DELETE /api/admin/contents/{contentId}
     */
    @DeleteMapping("/{contentId}")
    public ApiResponse<Void> deleteContent(@PathVariable Long contentId) {
        contentManagementService.deleteContent(contentId);
        
        return ApiResponse.<Void>builder()
                .code("ok")
                .message("Xóa nội dung thành công")
                .build();
    }

    /**
     * Lấy thống kê content
     * GET /api/admin/contents/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<ContentStatisticsResponse> getContentStatistics() {
        ContentStatisticsResponse statistics = contentManagementService.getContentStatistics();
        
        return ApiResponse.<ContentStatisticsResponse>builder()
                .code("ok")
                .message("Lấy thống kê nội dung thành công")
                .data(statistics)
                .build();
    }
}
