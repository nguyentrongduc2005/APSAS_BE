package com.project.apsas.service.impl;

import com.project.apsas.entity.User;
import com.project.apsas.integration.ImageCloudService;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarServiceImpl implements AvatarService {

    private final UserRepository userRepository;
    private final ImageCloudService imageCloudService;

    @Override
    public String getSmallAvatarUrl(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        String originalAvatarUrl = user.getAvatarUrl();
        if (originalAvatarUrl == null || originalAvatarUrl.isBlank()) {
            // Chưa có avatar thì tạm trả null (sau này bạn có thể đổi sang default URL)
            return null;
        }

        return imageCloudService.getOptimizedImageUrl(originalAvatarUrl, 100, 100);
    }
}
