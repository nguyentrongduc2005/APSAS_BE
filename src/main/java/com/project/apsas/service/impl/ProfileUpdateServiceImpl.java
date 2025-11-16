package com.project.apsas.service.impl;

import com.project.apsas.dto.request.UpdateProfileRequest;
import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.User;
import com.project.apsas.enums.Gender;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileUpdateServiceImpl implements ProfileUpdateService {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Override
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        // lấy id user hiện tại từ JWT
        String idStr = authService.currentId();
        Long userId = Long.parseLong(idStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        // cập nhật field cho phép sửa
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getDob() != null) {
            profile.setDob(request.getDob());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            try {
                Gender gender = Gender.valueOf(request.getGender().toUpperCase());
                profile.setGender(gender);
            } catch (IllegalArgumentException e) {
                // gửi gender linh tinh -> 400, không phải 500
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        }

        userRepository.save(user);   // cascade sang profile

        // build response, luôn check null để tránh NPE
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dob(profile.getDob())
                .bio(profile.getBio())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .gender(profile.getGender() != null ? profile.getGender().name() : null)
                .email(user.getEmail())
                .avatar(profile.getAvatarUrl())
                .build();
    }
}
