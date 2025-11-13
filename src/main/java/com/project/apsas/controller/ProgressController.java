package com.project.apsas.controller;

import com.project.apsas.dto.student.ProgressDTO;
import com.project.apsas.service.impl.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressController {

    private final UserService userService;


    @GetMapping("/{studentId}")
    public ProgressDTO getStudentProgress(@PathVariable Long studentId) {
        return userService.getStudentProgress(studentId);
    }

}
