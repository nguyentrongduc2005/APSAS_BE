package com.project.apsas.service.impl;

import com.project.apsas.entity.User;
import com.project.apsas.integration.ImageCloudService;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AvatarService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvatarServiceImpl implements AvatarService {

    private final UserRepository userRepository;
    private final ImageCloudService imageCloudService;

    public AvatarServiceImpl(UserRepository userRepository, ImageCloudService imageCloudService) {
        this.userRepository = userRepository;
        this.imageCloudService = imageCloudService;
    }

    /**
     * Retrieves the optimized URL for a user's small avatar (100x100).
     * If user or avatar is missing, throws exception.
     */
    @Override
    @Transactional(readOnly = true)
    public String getSmallAvatarUrl(Long userId) {
        // 1. Tìm user theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 2. Kiểm tra avatar gốc
        String originalAvatarUrl = user.getAvatarUrl(); // Giả sử User có trường này
        if (originalAvatarUrl == null || originalAvatarUrl.isEmpty()) {
            throw new IllegalStateException("User does not have an avatar uploaded.");
        }

        // 3. Tối ưu hóa URL với kích thước nhỏ
        String optimizedUrl = imageCloudService.getOptimizedImageUrl(originalAvatarUrl, 100, 100);

        // 4. Trả về URL đã tối ưu
        return optimizedUrl;
    }
}
