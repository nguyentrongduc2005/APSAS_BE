package com.project.apsas.integration;

import org.springframework.stereotype.Service;

@Service
public class ImageCloudService {

    public String getOptimizedImageUrl(String originalUrl, int width, int height) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }

        // Cloudinary: .../image/upload/...
        if (originalUrl.contains("/image/upload/")) {
            String marker = "/image/upload/";
            int idx = originalUrl.indexOf(marker);
            if (idx != -1) {
                int insertPos = idx + marker.length();
                String transformation = String.format("c_fill,w_%d,h_%d,g_face/", width, height);
                // Nếu đã có transform rồi thì trả luôn
                if (originalUrl.substring(insertPos).startsWith("c_")) {
                    return originalUrl;
                }
                return originalUrl.substring(0, insertPos)
                        + transformation
                        + originalUrl.substring(insertPos);
            }
        }

        // AWS S3 / CloudFront
        if (originalUrl.contains("amazonaws.com/") || originalUrl.contains("s3")) {
            String separator = originalUrl.contains("?") ? "&" : "?";
            return String.format("%s%sw=%d&h=%d&fit=crop", originalUrl, separator, width, height);
        }

        // Không nhận diện được thì trả nguyên URL
        return originalUrl;
    }
}
