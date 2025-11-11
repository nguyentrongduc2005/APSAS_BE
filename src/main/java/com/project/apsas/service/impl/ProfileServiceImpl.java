package com.project.apsas.service.impl;

import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.entity.User;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.CourseDetailService;
import com.project.apsas.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {
    UserRepository userRepo;
    @Override
    public ProfileResponse meFromJwt(Jwt jwt) {
        // lấy id từ claim. Tuỳ hệ thống: "sub", "userId", "uid" ...
        String raw = jwt.getClaimAsString("userId");
        if (raw == null) raw = jwt.getSubject(); // fallback "sub"
        Long userId = Long.valueOf(raw);

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Lấy roles từ token (scope/authorities) hoặc từ DB của bạn
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) roles = List.of(); // hoặc map từ u.getRole()

        return ProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .avatar(null) // điền nếu có cột
                .roles(roles)
                .build();
    }
}
