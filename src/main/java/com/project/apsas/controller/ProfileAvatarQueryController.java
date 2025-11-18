package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.SmallAvatarResponse;
import com.project.apsas.service.UserAvatarQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/me/avatar")
@RequiredArgsConstructor
public class ProfileAvatarQueryController {

    private final UserAvatarQueryService userAvatarQueryService;

    @GetMapping("/small")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SmallAvatarResponse> getMySmallAvatar() {
        SmallAvatarResponse data = userAvatarQueryService.getMySmallAvatar();
        return ApiResponse.<SmallAvatarResponse>builder()
                .code("ok")
                .message("SUCCESS")
                .data(data)
                .build();
    }

}
