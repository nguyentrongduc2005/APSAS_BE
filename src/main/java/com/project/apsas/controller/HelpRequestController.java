package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.HelpRequestResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.service.HelpRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/help-requests")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @GetMapping("/teacher/course/{courseId}")
    @PreAuthorize("hasAuthority('VIEW_HELP_REQUESTS')")
    public ResponseEntity<PagedResponse<HelpRequestResponse>> getHelpRequestsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<HelpRequestResponse> resp = helpRequestService.getHelpRequestsByCourse(courseId, page, limit);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('REQUEST_HELP')")
    public ApiResponse<HelpRequestResponse> createHelpRequest(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateHelpRequestRequest request
    ) {
        HelpRequestResponse response = helpRequestService.createHelpRequest(
                courseId, 
                request.getTitle(), 
                request.getBody()
        );
        
        return ApiResponse.<HelpRequestResponse>builder()
                .code("ok")
                .message("HELP_REQUEST_CREATED_SUCCESSFULLY")
                .data(response)
                .build();
    }

    @Data
    public static class CreateHelpRequestRequest {
        @NotBlank(message = "Title is required")
        private String title;
        
        @NotBlank(message = "Body is required")
        private String body;
    }
}
