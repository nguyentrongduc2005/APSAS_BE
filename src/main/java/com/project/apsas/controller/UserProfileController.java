package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/me/avatar")   // cho đúng style với /me, /me (update profile)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    private final CloudinaryService cloudinaryService;   // đã có sẵn
    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping
    public ApiResponse<String> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // currentId() trả về subject trong JWT => String, parse sang Long
            Long userId = Long.parseLong(authService.currentId());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Lấy profile, nếu null thì tạo mới (vì Profile là @OneToOne với User)
            Profile profile = user.getProfile();
            if (profile == null) {
                profile = Profile.builder()
                        .user(user)
                        .build();
                user.setProfile(profile);
            }

            String publicId = "user_" + userId + "_avatar";

            UploadResult uploadResult = cloudinaryService.upload(
                    file,
                    "avatars",
                    publicId
            );

            // Lưu URL vào profile.avatarUrl
            profile.setAvatarUrl(uploadResult.getUrl());

            // Save user (cascade xuống profile)
            userRepository.save(user);

            return ApiResponse.<String>builder()
                    .code("OK")
                    .message("Upload avatar thành công")
                    .data(uploadResult.getUrl())
                    .build();

        } catch (NumberFormatException e) {
            // currentId không parse được sang Long
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        } catch (IOException e) {
            // Lỗi khi upload lên Cloudinary
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
