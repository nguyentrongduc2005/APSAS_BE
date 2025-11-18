package com.project.apsas.controller;

import com.project.apsas.dto.response.HelpRequestResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.service.HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher/help-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VIEW_HELP_REQUESTS')")
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<PagedResponse<HelpRequestResponse>> getHelpRequestsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<HelpRequestResponse> resp = helpRequestService.getHelpRequestsByCourse(courseId, page, limit);
        return ResponseEntity.ok(resp);
    }

}
