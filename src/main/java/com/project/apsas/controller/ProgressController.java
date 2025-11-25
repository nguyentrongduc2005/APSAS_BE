package com.project.apsas.controller;

import com.cloudinary.Api;
import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.student.DailyScoreDTO;
import com.project.apsas.dto.student.ProgressDTO;
import com.project.apsas.service.impl.UserService;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ApiResponse<ProgressDTO> getCurrentProgress(@PathVariable Long studentId) {
        ProgressDTO progressDTO =  userService.getStudentCurrentProgress(studentId);


         return ApiResponse.<ProgressDTO>builder().code("OK")
                .message("Lấy progress hiện tại thành công")
                .data(progressDTO)
                .build();
    }

//    @PostMapping("/login")
//    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
//        LoginResponse res = authService.login(request);
//        return ApiResponse.<LoginResponse>builder()
//                .code("OK")
//                .message("Đăng nhập thành công")
//                .data(res)
//                .build();
//    }

    /**
     * API 2:
     * Lấy DailyScore theo khoảng ngày (<= 30 ngày)
     * Example:
     * /progress/3/scores?from=2025-01-01&to=2025-01-10
     */
    @GetMapping("/{studentId}/scores")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ApiResponse<List<DailyScoreDTO>> getDailyScores(
            @PathVariable Long studentId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        List<DailyScoreDTO> scores = userService.getStudentDailyScores(studentId, from, to);
        return ApiResponse.<List<DailyScoreDTO>>builder()
                .code("OK")
                .message("Lấy điểm hàng ngày thành công")
                .data(scores)
                .build();
    }
}

