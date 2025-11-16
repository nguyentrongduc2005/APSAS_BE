package com.project.apsas.service;

import com.project.apsas.dto.request.UpdateProfileRequest;
import com.project.apsas.dto.response.ProfileResponse;

public interface ProfileUpdateService {
    ProfileResponse updateMyProfile(UpdateProfileRequest request);
}
