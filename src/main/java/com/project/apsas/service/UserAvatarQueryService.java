package com.project.apsas.service;

import com.project.apsas.dto.response.SmallAvatarResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserAvatarQueryService {

    SmallAvatarResponse getMySmallAvatar();

}
