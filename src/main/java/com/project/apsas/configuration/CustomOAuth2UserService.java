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
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");

        log.info("OAuth2: Processing user {}", email);

        Optional<User> userOptional = userRepository.findByEmailWithRoles(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("OAuth2: Found existing user {} with roles: {}", email,
                    user.getRoles().stream().map(Role::getName).toList());

            user.setName(name);
            if (user.getProfile() != null) {
                user.getProfile().setAvatarUrl(avatarUrl);
            } else {
                Profile profile = Profile.builder()
                        .dob(null)
                        .phone(null)
                        .bio(null)
                        .address(null)
                        .avatarUrl(avatarUrl)
                        .gender(null)
                        .user(user)
                        .build();
                user.setProfile(profile);
            }
            user = userRepository.save(user);
        } else {
            log.info("OAuth2: Creating new user {}", email);

            Role studentRole = roleRepository.findByName(com.project.apsas.enums.Role.STUDENT.name())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRoles(Set.of(studentRole));
            user.setStatus(UserStatus.ACTIVE);
            user.setPassword(UUID.randomUUID().toString());

            Profile profile = Profile.builder()
                    .dob(null)
                    .phone(null)
                    .bio(null)
                    .address(null)
                    .avatarUrl(avatarUrl)
                    .gender(null)
                    .user(user)
                    .build();
            user.setProfile(profile);

            user = userRepository.save(user);

            Progress progress = Progress.builder()
                    .userId(user.getId())
                    .totalAttemptNo(0)
                    .acceptance(0.0f)
                    .build();
            progressRepository.save(progress);

            user = userRepository.findByIdWithRoles(user.getId())
                    .orElseThrow(() -> new RuntimeException("Failed to reload user"));

            log.info("OAuth2: Created user {} with roles: {}", email,
                    user.getRoles().stream().map(Role::getName).toList());
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                log.info("OAuth2: Added role authority: ROLE_{}", role.getName());

                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                        log.info("OAuth2: Added permission authority: {}", permission.getName());
                    });
                }
            });
        }

        log.info("OAuth2: Final authorities for user {}: {}", email, authorities);

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                authorities,
                attributes,
                "email"
        );
    }
}