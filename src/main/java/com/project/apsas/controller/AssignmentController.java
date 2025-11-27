package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.assignment.AssignmentDetailDTO;
import com.project.apsas.dto.request.assignment.AssignmentListItemDTO;
import com.project.apsas.dto.request.assignment.SetTimeRequest;
import com.project.apsas.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentController {
    AssignmentService assignmentService;
    @PreAuthorize("hasAuthority('CREATE_COURSE')")
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
    @GetMapping("/{courseId}/assignments")
    @PreAuthorize("hasAuthority('VIEW_COURSES')")
    public ApiResponse<List<AssignmentListItemDTO>> getAssignmentsByCourse(
            @PathVariable Long courseId) {

        List<AssignmentListItemDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId);

        return ApiResponse.<List<AssignmentListItemDTO>>builder()
                .code("ok")
                .message("ASSIGNMENTS_RETRIEVED_SUCCESSFULLY")
                .data(assignments)
                .build();
    }

    @GetMapping("/{courseId}/assignments/{assignmentId}")
    @PreAuthorize("hasAuthority('VIEW_COURSES')")
    public ApiResponse<AssignmentDetailDTO> getAssignmentDetail(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId) {

        AssignmentDetailDTO assignmentDetail = assignmentService
                .getAssignmentDetail(courseId, assignmentId);

        return ApiResponse.<AssignmentDetailDTO>builder()
                .code("ok")
                .message("ASSIGNMENT_DETAIL_RETRIEVED_SUCCESSFULLY")
                .data(assignmentDetail)
                .build();
    }
}
