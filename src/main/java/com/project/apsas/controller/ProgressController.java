package com.project.apsas.controller;

import com.project.apsas.dto.student.DailyScoreDTO;
import com.project.apsas.dto.student.ProgressDTO;
import com.project.apsas.service.impl.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressController {

    UserService userService;

    /**
     * API 1:
     * Lấy progress hiện tại (7 ngày gần nhất)
     */
    @GetMapping("/{studentId}/current")
    public ProgressDTO getCurrentProgress(@PathVariable Long studentId) {
        return userService.getStudentCurrentProgress(studentId);
    }


    /**
     * API 2:
     * Lấy DailyScore theo khoảng ngày (<= 30 ngày)
     * Example:
     * /progress/3/scores?from=2025-01-01&to=2025-01-10
     */
    @GetMapping("/{studentId}/scores")
    public List<DailyScoreDTO> getDailyScores(
            @PathVariable Long studentId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return userService.getStudentDailyScores(studentId, from, to);
    }
}

