package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.UpdateProfileRequest;
import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.service.ProfileUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfileEditController {

    private final ProfileUpdateService profileUpdateService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request
    ) {
        var data = profileUpdateService.updateMyProfile(request);
        return ApiResponse.<ProfileResponse>builder()
                .code("OK")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}