package com.project.apsas.service;


import com.project.apsas.dto.response.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CloudinaryService {
        public String getAvatarThumbnailUrl(String publicId);
        public UploadResult upload(MultipartFile file, String folder, String publicId) throws IOException;
        public String getResizedUrl(String publicId, int width, int height);
        public Map delete(String publicId, String resourceType) throws IOException;
        public Map deleteFiles(List<String> publicIds) throws Exception;
}
