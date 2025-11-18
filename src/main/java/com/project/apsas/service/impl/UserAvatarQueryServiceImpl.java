package com.project.apsas.service.impl;

import com.project.apsas.dto.response.SmallAvatarResponse;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ProfileRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CloudinaryService;
import com.project.apsas.service.UserAvatarQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAvatarQueryServiceImpl implements UserAvatarQueryService {

    AuthService authService;
    UserRepository userRepository;
    ProfileRepository profileRepository;
    CloudinaryService cloudinaryService;

    @NonFinal
    @Value("${cloudinary.option.folder-name}")
    String folder;

    @Override
    public SmallAvatarResponse getMySmallAvatar() {
        String idStr = authService.currentId();
        if (!StringUtils.hasText(idStr)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Long userId;
        try {
            userId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Profile profile = user.getProfile();

        if (profile == null || !StringUtils.hasText(profile.getAvatarUrl())) {
            return SmallAvatarResponse.builder()
                    .userId(userId)
                    .name(user.getName())
                    .email(user.getEmail())
                    .avatarUrl(null)
                    .smallAvatarUrl(null)
                    .success(false)
                    .build();
        }

        String avatarUrl = profile.getAvatarUrl();
        String smallAvatarUrl = buildSmallAvatarUrlFromOriginal(avatarUrl);

        return SmallAvatarResponse.builder()
                .userId(userId)
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(avatarUrl)
                .smallAvatarUrl(smallAvatarUrl)
                .success(true)
                .build();
    }

//    @Override
//    public SmallAvatarResponse uploadMyAvatar(MultipartFile file) throws IOException {
//        if (file == null || file.isEmpty()) {
//            throw new AppException(ErrorCode.VALIDATION_FAILED);
//        }
//
//        String idStr = authService.currentId();
//        if (!StringUtils.hasText(idStr)) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//
//        Long userId;
//        try {
//            userId = Long.parseLong(idStr);
//        } catch (NumberFormatException e) {
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        Profile profile = user.getProfile();
//        if (profile == null) {
//            profile = new Profile();
//            profile.setUser(user);
//        }
//
//        String avatarUrl = null;
//        String smallAvatarUrl = null;
//        boolean success = false;
//
//        String publicId = UUID.randomUUID().toString();
//        try {
//            UploadResult uploadResult = cloudinaryService.upload(file, folder, publicId);
//            avatarUrl = uploadResult.getUrl();
//            profile.setAvatarUrl(avatarUrl);
//            success = true;
//
//            if (uploadResult.getPublicId() != null) {
//                smallAvatarUrl = cloudinaryService.getAvatarThumbnailUrl(uploadResult.getPublicId());
//            } else {
//                smallAvatarUrl = buildSmallAvatarUrlFromOriginal(avatarUrl);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        profileRepository.save(profile);
//
//        return SmallAvatarResponse.builder()
//                .userId(userId)
//                .name(user.getName())
//                .email(user.getEmail())
//                .avatarUrl(avatarUrl)
//                .smallAvatarUrl(smallAvatarUrl)
//                .success(success)
//                .build();
//    }

    private String buildSmallAvatarUrlFromOriginal(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return null;
        }

        String publicId = extractPublicIdFromUrl(avatarUrl);
        if (!StringUtils.hasText(publicId)) {
            return avatarUrl;
        }

        return cloudinaryService.getAvatarThumbnailUrl(publicId);
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = url.substring(uploadIndex + "/upload/".length());

            if (afterUpload.startsWith("v")) {
                int slashIndex = afterUpload.indexOf('/');
                if (slashIndex > 1) {
                    String versionPart = afterUpload.substring(1, slashIndex);
                    boolean allDigits = true;
                    for (int i = 0; i < versionPart.length(); i++) {
                        if (!Character.isDigit(versionPart.charAt(i))) {
                            allDigits = false;
                            break;
                        }
                    }
                    if (allDigits) {
                        afterUpload = afterUpload.substring(slashIndex + 1);
                    }
                }
            }

            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex > 0) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }

            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
