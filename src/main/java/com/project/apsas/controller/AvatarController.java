package com.project.apsas.controller;

import com.project.apsas.service.AvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing and retrieving user Avatars.
 * Authentication is required for all endpoints.
 */
@RestController
@RequestMapping("/api/v1")
public class AvatarController {

    // Sử dụng Interface AvatarService
    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    /**
     * Endpoint to get the small avatar URL of a user by ID.
     * The small size is defaulted to 100x100 pixels.
     * * @param userId The ID of the user whose avatar is requested.
     * @return ResponseEntity containing the URL of the small avatar image.
     */
    @PreAuthorize("isAuthenticated()") // Ensure only authenticated users can call this API.
    @GetMapping("/users/{userId}/avatar/small")
    public ResponseEntity<String> getSmallAvatarUrl(@PathVariable Long userId) {
        // Call the service layer to handle the logic for fetching and caching the image URL.
        String smallAvatarUrl = avatarService.getSmallAvatarUrl(userId);

        // Return the result (ResponseEntity.ok() automatically returns status 200 OK)
        return ResponseEntity.ok(smallAvatarUrl);
    }
}