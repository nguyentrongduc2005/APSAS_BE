package com.project.apsas.enums;

import org.springframework.web.multipart.MultipartFile;

public enum MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LINK
    ;

    private static final org.springframework.http.MediaType IMAGE_WILDCARD =
            org.springframework.http.MediaType.valueOf("image/*");

    private static final org.springframework.http.MediaType VIDEO_WILDCARD =
            org.springframework.http.MediaType.valueOf("video/*");


    public static MediaType fromImageOrVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getContentType() == null) {
            return null; // Bỏ qua file rỗng hoặc không có content type
        }

        String contentType = file.getContentType();

        try {
            // Dùng class MediaType của Spring
            org.springframework.http.MediaType springMediaType =
                    org.springframework.http.MediaType.valueOf(contentType);

            // SỬA LỖI Ở ĐÂY: Dùng hằng số IMAGE_WILDCARD mà chúng ta tự tạo
            if (springMediaType.isCompatibleWith(IMAGE_WILDCARD)) {
                return IMAGE;
            }

            // SỬA LỖI Ở ĐÂY: Dùng hằng số VIDEO_WILDCARD mà chúng ta tự tạo
            if (springMediaType.isCompatibleWith(VIDEO_WILDCARD)) {
                return VIDEO;
            }

            // Không phải ảnh, cũng không phải video
            return null; // Bỏ qua file này

        } catch (Exception e) {
            // ContentType bị lỗi, không thể parse
            return null; // Bỏ qua file này
        }
    }
}
