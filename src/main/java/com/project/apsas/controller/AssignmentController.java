package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.assignment.SetTimeRequest;
import com.project.apsas.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/assignment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentController {

    AssignmentService assignmentService;
    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/{assignmentId}/course/{courseId}/set-time")
    public ApiResponse<String> setTime(
            @PathVariable Long assignmentId,
            @PathVariable Long courseId,
            @Valid SetTimeRequest request
            ) {

        assignmentService.setTime(assignmentId, courseId, request.getOpenAt(), request.getDueAt());
        return ApiResponse.<String>builder()
                .code("ok")
                .message("ok")
                .data("đã set thời gian thành công")
                .build();
    }

}
