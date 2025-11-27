package com.project.apsas.controller;

import com.project.apsas.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/{userId}/avatar/small")
    public ResponseEntity<String> getSmallAvatarUrl(@PathVariable Long userId) {
        String smallAvatarUrl = avatarService.getSmallAvatarUrl(userId);
        return ResponseEntity.ok(smallAvatarUrl);
    }
}
