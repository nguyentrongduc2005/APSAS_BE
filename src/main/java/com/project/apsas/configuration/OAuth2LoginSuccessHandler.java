package com.project.apsas.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.apsas.entity.RefreshToken;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final  PasswordEncoder passwordEncoder; // Dùng để hash refresh token

    // --- Copy các giá trị @Value từ AuthServiceImpl ---
    @Value("${jwt.signerKey}")
    private String jwtSecret;

    @Value("${app.name}")
    private String jwtIssuer;

    @Value("${jwt.verify.ttl-minutes}")
    private long accessTtlMinutes;

    @Value("${jwt.refresh.ttl-minutes}")
    private long refreshTtlMinutes;

    @Value("${spring.frontend.url}")
    private String frontendUrl;
    // ---

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 Success Handler: Processing for email {}", email);

        // 1. Lấy User từ DB kèm Roles (Sử dụng hàm có JOIN FETCH)
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // 2. Tạo Token
        String accessToken = generateAccessToken(user);
        String rf = generateRefreshToken();

        // 3. Xử lý Refresh Token (Lưu vào DB)
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.now().plus(refreshTtlMinutes, ChronoUnit.MINUTES), ZoneId.systemDefault());
        if (Objects.isNull(user.getRefreshToken())) {
            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(passwordEncoder.encode(rf))
                    .expiresAt(expiresAt)
                    .userId(user.getId())
                    .build();
            user.setRefreshToken(refreshToken);
        } else {
            user.getRefreshToken().setTokenHash(passwordEncoder.encode(rf));
            user.getRefreshToken().setExpiresAt(expiresAt);
        }
        userRepository.save(user);

        // 4. Xử lý lấy Roles để trả về URL (FIX LỖI EMPTY ROLE)
        String userRoles = "";
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            // Cách 1: Lấy từ DB nếu có
            userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(","));
            log.info("OAuth2: Roles found in DB: {}", userRoles);
        } else {
            // Cách 2 (Fallback): Nếu DB trả về rỗng (do lazy loading hoặc lỗi cache), lấy từ Authentication Context
            // Vì CustomOAuth2UserService đã nạp quyền vào Authentication rồi
            log.warn("OAuth2: Roles empty in DB entity, falling back to Authentication authorities");
            userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replace("ROLE_", "")) // Bỏ tiền tố ROLE_ để giống format FE cần
                    .collect(Collectors.joining(","));
        }

        String avatarUrl = (user.getProfile() != null && user.getProfile().getAvatarUrl() != null)
                ? user.getProfile().getAvatarUrl() : "";

        // 5. Build Redirect URL
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", rf)
                .queryParam("userId", user.getId())
                .queryParam("userName", URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .queryParam("userEmail", user.getEmail())
                .queryParam("userRoles", userRoles) // Param này sẽ chứa "STUDENT" hoặc "ADMIN"
                .queryParam("userAvatar", avatarUrl)
                .build().toUriString();

        log.info("OAuth2: Redirecting to Frontend with Roles: {}", userRoles);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
    // --- Copy các hàm private từ AuthServiceImpl ---
    private String generateAccessToken(User user) {
        try {
            JWSSigner signer = new MACSigner(jwtSecret.getBytes());
            Date now = new Date();
            Date exp = Date.from(Instant.now().plus(accessTtlMinutes, ChronoUnit.MINUTES));

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.getId()))
                    .issuer(jwtIssuer)
                    .issueTime(now)
                    .expirationTime(exp)
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("scope", buildScope(user))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS512),
                    claims
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Generate access token error", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}