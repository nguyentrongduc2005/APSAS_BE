package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("returnObject.data.id.toString() == authentication.name")
    public ApiResponse<ProfileResponse> me() {
        var data = profileService.meFromJwt();
        return ApiResponse.<ProfileResponse>builder()
                .code("ok")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
