package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("assigment")
@RequiredArgsConstructor
public class AssignmentController {
    @PreAuthorize("hasRole('LECTURE')")
    @PostMapping("/set-time")
    public ApiResponse<String> setTime(@RequestParam("openAt") LocalDateTime openAt,
                                       @RequestParam("dueAt") LocalDateTime dueAt) {
        return ApiResponse.<String>builder()
                .code("ok")
                .message("ok")
                .data("")
                .build();
    }

}
