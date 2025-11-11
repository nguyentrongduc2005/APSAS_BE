package com.project.apsas.service;

import com.project.apsas.dto.response.ProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ProfileService {
    ProfileResponse me(Jwt jwt);
}
