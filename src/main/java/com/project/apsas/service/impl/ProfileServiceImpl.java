package com.project.apsas.service.impl;

import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public ProfileResponse meFromJwt(Jwt jwt) {
        String idStr = jwt.getClaimAsString("userId");
        if (idStr == null || idStr.isBlank()) idStr = jwt.getSubject();
        if (idStr == null || idStr.isBlank()) throw new AppException(ErrorCode.UNAUTHORIZED);

        final long userId;
        try { userId = Long.parseLong(idStr); }
        catch (NumberFormatException e) { throw new AppException(ErrorCode.UNAUTHORIZED); }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) roles = List.of();

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(null)         // map nếu có field/avatar trong DB
                .roles(roles)
                .build();
    }
}
