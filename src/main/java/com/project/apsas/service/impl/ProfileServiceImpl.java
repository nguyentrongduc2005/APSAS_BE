package com.project.apsas.service.impl;

import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public ProfileResponse meFromJwt() {

        String idStr = authService.currentId();
        Long userId;
        try {
            userId = Long.parseLong(idStr);

        } catch (NumberFormatException e) { throw new AppException(ErrorCode.UNAUTHORIZED); }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Profile profile = user.getProfile();

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dob(profile.getDob())
                .bio(profile.getBio())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .gender(profile.getGender() == null
                    ? ""
                        : profile.getGender().name()
                        )
                .email(user.getEmail())
                .avatar(profile.getAvatarUrl())         // map nếu có field/avatar trong DB
                .build();
    }
}
