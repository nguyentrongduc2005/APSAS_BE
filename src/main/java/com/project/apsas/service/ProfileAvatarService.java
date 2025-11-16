package com.project.apsas.service;

import com.project.apsas.dto.response.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileAvatarService {

    /**
     * Upload avatar mới cho user hiện tại và trả về profile kèm avatar mới.
     */
    ProfileResponse updateMyAvatar(MultipartFile file) throws IOException;
}
