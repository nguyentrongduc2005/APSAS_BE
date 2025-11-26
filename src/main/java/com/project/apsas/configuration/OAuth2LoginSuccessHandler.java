package com.project.apsas.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.apsas.entity.RefreshToken;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        // 1. Lấy user đã được load bởi CustomOAuth2UserService
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2: Handling success for user {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // 2. Tạo AccessToken và RefreshToken (Bắt chước hàm login)
        String accessToken = generateAccessToken(user); // Copy từ AuthServiceImpl
        String rf = generateRefreshToken(); // Copy từ AuthServiceImpl

        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.now().plus(refreshTtlMinutes, ChronoUnit.MINUTES),
                ZoneId.systemDefault());

        // 3. Lưu RefreshToken (Bắt chước hàm login)
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
        userRepository.save(user); // Lưu user (cùng với refresh token)

        // 4. Xây dựng URL Redirect về Frontend
        // Gửi token qua query param
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", rf)
                .build().toUriString();

        log.info("OAuth2: Redirecting to {}", redirectUrl);

        // 5. Thực hiện redirect
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