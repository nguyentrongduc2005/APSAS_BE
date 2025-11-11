package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        var data = profileService.me(jwt);
        return ApiResponse.<ProfileResponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
