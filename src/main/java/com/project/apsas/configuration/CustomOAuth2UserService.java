package com.project.apsas.configuration;

import com.project.apsas.entity.Profile;
import com.project.apsas.entity.Progress;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ProgressRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy thông tin user
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");

        // 2. Logic "Find or Create" (Bắt chước hàm register của bạn)
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // User đã tồn tại -> Cập nhật tên và avatar
            user = userOptional.get();
            log.info("OAuth2: Found existing user {}", email);
            user.setName(name);
            if (user.getProfile() != null) {
                user.getProfile().setAvatarUrl(avatarUrl);
            } else {
                // Nếu profile bị thiếu (lỗi data cũ), tạo mới
                Profile profile = Profile.builder().user(user).avatarUrl(avatarUrl).build();
                user.setProfile(profile);
            }
            user = userRepository.save(user);
        } else {
            // User mới -> Tạo user mới (Bắt chước hàm register)
            log.info("OAuth2: Creating new user {}", email);

            // Lấy role STUDENT
            Role studentRole = roleRepository.findByName(com.project.apsas.enums.Role.STUDENT.name())
                    .orElseThrow((() -> new RuntimeException("Error: Role STUDENT is not found.")));

            userRepository.findByEmail(email).ifPresent(u -> {
                throw new AppException(ErrorCode.USER_ESIXSTED);
            });
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRoles(Set.of(studentRole));
            user.setStatus(UserStatus.ACTIVE); // Kích hoạt luôn
            // Set mật khẩu ngẫu nhiên vì user sẽ không dùng nó
            user.setPassword(UUID.randomUUID().toString());

            // Tạo Profile (giống register)
            Profile profile = Profile.builder()
                    .avatarUrl(avatarUrl) // Dùng avatar từ Google
                    .user(user)
                    .build();
            user.setProfile(profile);

            // Lưu user trước để lấy ID
            User savedUser = userRepository.save(user);

            // Tạo Progress (giống register)
            Progress progress = Progress.builder()
                    .userId(savedUser.getId())
                    .totalAttemptNo(0)
                    .acceptance(0.0f)
                    .build();
            progressRepository.save(progress);

            user = savedUser;
        }

        // 3. Trả về Principal cho Spring
        // Chúng ta sẽ dùng "email" làm định danh
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    });
                }
            });
        }

        // Chúng ta sẽ dùng "email" làm định danh
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                authorities, // Dùng danh sách quyền vừa tạo
                attributes,
                "email" // Dùng email làm 'name' attribute key
        );
    }
}