package com.project.apsas.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Integration Service for interacting with cloud storage and image processing services
 * (e.g., Cloudinary, AWS S3/Cloudfront/Lambda, etc.).
 * This class is placed in its own 'cloud' sub-package within 'integration'.
 */
@Service
public class ImageCloudService {
    // ... (logic không đổi)

    /**
     * Generates an optimized image URL with specified width and height.
     * This method assumes a specialized CDN/Service (like Cloudinary) is used
     * for resizing via URL parameters.
     * * @param originalUrl The original image URL (from S3 or database).
     * @param width The desired width.
     * @param height The desired height.
     * @return The optimized image URL.
     */
    public String getOptimizedImageUrl(String originalUrl, int width, int height) {

        // --- PREFERRED METHOD: USING CLOUDINARY (Assumes you have CloudinaryConfig) ---
        if (originalUrl.contains("cloudinary")) {
            // Transformation parameters (e.g., /c_fill,h_100,w_100/)
            String transformation = String.format("c_fill,w_%d,h_%d,g_face/", width, height);

            // Find the insertion point (e.g., after /upload/)
            int insertIndex = originalUrl.indexOf("/upload/");
            if (insertIndex != -1) {
                // Insert the transformation parameter into the Cloudinary URL
                return originalUrl.substring(0, insertIndex + 8) + transformation + originalUrl.substring(insertIndex + 8);
            }
        }

        // --- FALLBACK METHOD: S3 + AWS Lambda/Cloudfront ---
        if (originalUrl.contains("amazonaws.com/") || originalUrl.contains("s3")) {
            // Example: Append parameters to the S3 URL for Cloudfront/Lambda to process
            return String.format("%s?w=%d&h=%d&fit=crop", originalUrl, width, height);
        }

        // If the URL is not recognizable or cannot be optimized, return the original URL
        return originalUrl;
    }
}