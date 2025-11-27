package com.project.apsas.service;

/**
 * Interface definition for Avatar service operations.
 * Handles the business logic related to fetching, caching, and optimizing avatar URLs.
 */
public interface AvatarService {

    /**
     * Retrieves the optimized URL for a user's small avatar.
     * This method is typically annotated with @Cacheable in the implementation class.
     * * @param userId The ID of the user.
     * @return The optimized and cached URL of the small avatar image.
     */
    String getSmallAvatarUrl(Long userId);
}