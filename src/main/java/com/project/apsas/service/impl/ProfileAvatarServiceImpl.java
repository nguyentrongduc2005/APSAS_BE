package com.project.apsas.service.impl;

import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.dto.response.UploadResult;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ProfileRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CloudinaryService;
import com.project.apsas.service.ProfileAvatarService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileAvatarServiceImpl implements ProfileAvatarService {

    AuthService authService;
    UserRepository userRepository;
    ProfileRepository profileRepository;
    CloudinaryService cloudinaryService;
    @NonFinal
    @Value("${cloudinary.option.folder-name}")
    String folder;

    @Override
    public ProfileResponse updateMyAvatar(MultipartFile file) throws IOException {
        // 1. Kiểm tra file hợp lệ
        if (file == null || file.isEmpty()) {
            // TODO: Nếu trong ErrorCode của bạn có mã nào hợp với validate sai,
            // hãy sửa lại cho đúng. Tạm thời dùng VALIDATION_FAILED (hoặc BAD_REQUEST tuỳ enum của bạn).
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }

        // 2. Lấy id user hiện tại từ token (AuthService đã có sẵn trong project)
        Long userId;
        try {
            userId = Long.parseLong(authService.currentId());
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3. Lấy user & profile từ DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
        }

        // 4. Upload lên Cloudinary
        // Nếu trong application.yaml có folder-name riêng thì bạn có thể truyền vào
        // Ở đây ví dụ: folder = "avatars"
        String publicId = UUID.randomUUID().toString();
        boolean success = false;
        try {
            UploadResult uploadResult = cloudinaryService.upload(file, folder, publicId);
            profile.setAvatarUrl(uploadResult.getUrl());
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 5. Lưu URL avatar vào profile

        profileRepository.save(profile);

        // 6. Build ProfileResponse trả về giống các API profile khác
        return ProfileResponse.builder()
                .avatar(profile.getAvatarUrl())
                .success(success)
                .build();
    }
}
