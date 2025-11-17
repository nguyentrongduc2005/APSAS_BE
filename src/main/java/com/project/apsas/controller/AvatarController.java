package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// ❗ Không import ApiResponse ở đây
// ❗ Chút nữa bạn sẽ dùng Alt + Enter để IntelliJ tự import đúng package

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    // ======================
    //  API UPLOAD AVATAR
    // ======================
    @PostMapping
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ApiResponse.<String>builder()
                    .code("BAD_REQUEST")
                    .message("File upload trống")
                    .data(null)
                    .build();
        }

        // Lấy tên file (tạm)
        String fileName = file.getOriginalFilename();

        return ApiResponse.<String>builder()
                .code("OK")
                .message("UPLOAD_AVATAR_SUCCESS")
                .data(fileName)
                .build();
    }

    // ======================
    //  API GET AVATAR
    // ======================
    @GetMapping
    public ApiResponse<String> getAvatar() {
        return ApiResponse.<String>builder()
                .code("OK")
                .message("GET_AVATAR_SUCCESS")
                .data("Avatar URL sẽ trả ở đây sau này")
                .build();
    }
}
