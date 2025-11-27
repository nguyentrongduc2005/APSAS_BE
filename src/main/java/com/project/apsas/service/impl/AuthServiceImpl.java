package com.project.apsas.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.dto.request.*;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;

import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.entity.*;

import com.project.apsas.enums.UserStatus;

import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.*;

import com.project.apsas.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final ProgressRepository progressRepository;

    UserRepository userRepository;
    RoleRepository roleRepository;
    OtpRepository otpRepository;
    PasswordEncoder passwordEncoder;
    UserMapper mapper;
    KafkaMailProducer mailProducer;
    RefreshTokenRepository  refreshTokenRepository;

    BasicRedisServiceImpl redisService;
    @NonFinal
    @Value("${jwt.signerKey}")
    String jwtSecret;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    @NonFinal

    @Value("${app.name}")
    String jwtIssuer;
    @NonFinal
    @Value("${jwt.verify.ttl-minutes}")
    long accessTtlMinutes;

    @NonFinal
    @Value("${jwt.refresh.ttl-minutes}")
    long refreshTtlMinutes;
    @NonFinal
    @Value("${message-queue.topic.mail.name}")
    String TOPIC_MAIL;

    int VERIFY_TTL_MINUTES = 10;

    // ================= REGISTER =================
    @Override
    public RegisterResponse register(RegisterRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(email).ifPresent(u -> {
            throw new AppException(ErrorCode.USER_ESIXSTED);
        });
        String roleName = "";
        if(req.getRole() == 1)  roleName = com.project.apsas.enums.Role.STUDENT.name();
        if(req.getRole() == 2)  roleName = com.project.apsas.enums.Role.LECTURER.name();

        Role role = roleRepository.findByName(roleName).orElseThrow(() ->
                new AppException(ErrorCode.BAD_REQUEST));

        String hashed = passwordEncoder.encode(req.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setName(req.getName());
        user.setRoles(Set.of(role));
        user.setStatus(UserStatus.INACTIVE);


        Profile  profile = Profile.builder()
                .dob(null)
                .phone(null)
                .bio(null)
                .address(null)
                .avatarUrl(null)
                .gender(null)
                .user(user)
                .build();
        user.setProfile(profile);


        // Tạo OTP
        String code = genOtp6();
        String otpkey = "otp:" + email;
        redisService.set(otpkey, code);
        redisService.setTimeToLive(otpkey, VERIFY_TTL_MINUTES * 60);

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES),
                ZoneId.systemDefault()
        );



        Otp otp = Otp.builder()
                .code(code)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        user.setOtp(otp);  // Set otp cho user

        // Save user (sẽ tự động save otp nếu có cascade)
        userRepository.save(user);


        Progress progress = Progress.builder()
                .userId(user.getId())
                .totalAttemptNo(0)
                .acceptance(0.0f)
                .build();

        progressRepository.save(progress);



        sendOtpMail(email, user.getName(), code, VERIFY_TTL_MINUTES);

        return RegisterResponse.builder()
                .email(user.getEmail())
                .id(user.getId())
                .role(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", "))
                )
                .build();
    }

    @Override
    public void verify(VerifyRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_ESIXSTED));

        String otpKey = "OTP:" + request.getEmail();
        Object storedOtp = redisService.Get(otpKey);
        if (storedOtp == null || !storedOtp.equals(request.getCode())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }


        String userCode = user.getOtp().getCode();
        if(!(user.getStatus().equals(UserStatus.INACTIVE)
                && request.getCode().equals(userCode))) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        redisService.delete(otpKey);
    }

    // ================= LOGIN =================
    @Override
    public LoginResponse login(LoginRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String accessToken = generateAccessToken(user);
        String rf = generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.now().plus(refreshTtlMinutes, ChronoUnit.MINUTES),
                ZoneId.systemDefault()) ;
        // Trả về theo mapper hiện có của bạn
        LoginResponse res = mapper.toLoginResponse(user);
        if(Objects.isNull(user.getRefreshToken())) {
            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenHash(passwordEncoder.encode(rf))
                    .expiresAt(expiresAt)
                    .userId(user.getId())
                    .build();
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        } else {
            user.getRefreshToken().setTokenHash(passwordEncoder.encode(rf));
            user.getRefreshToken().setExpiresAt(expiresAt);
        }

        res.setAccessToken(accessToken);
        res.setRefreshToken(rf);

        return res;
    }

    // ================= RESEND OTP =================
    @Override
    public void resendCode(ResendCodeRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_ESIXSTED));

        String code = genOtp6();
        String otpKey = "OTP:" + email;
        redisService.set(otpKey, code);
        redisService.setTimeToLive(otpKey, VERIFY_TTL_MINUTES * 60);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES),
                ZoneId.systemDefault());
        user.getOtp().setCode(code);
        userRepository.save(user);

        sendOtpMail(email, user.getName(), code, VERIFY_TTL_MINUTES);
    }

    // ================= Helpers =================
    private String genOtp6() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void sendOtpMail(String to, String name, String otp, int minutesLeft) {
        Properties params = new Properties();
        params.setProperty("otp_code", otp); // Mã OTP giả
        params.setProperty("action", "kiểm tra hệ thống"); // Hành động
        params.setProperty("expiry_minutes", String.valueOf(minutesLeft));
        SendMailEvent event = SendMailEvent.builder()
                .toEmail(to)
                .name(name)
                .params(params)
                .build();
        mailProducer.push(TOPIC_MAIL, event.getToEmail(), event);
    }
    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

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

    // ================= INTROSPECT =================
    @Override
    public IntrospecResponse introspect(IntrospectRequest request) {
        boolean valid = true;

        try {
            String token = request.getToken();

            // Kiểm tra token có null hoặc empty không
            if (token == null || token.trim().isEmpty()) {
                return IntrospecResponse.builder()
                        .valid(false)
                        .build();
            }

            // Verify token
            verifyToken(token, false);

        } catch (JOSEException e) {
            log.error("Token signature verification failed: {}", e.getMessage());
            valid = false;
        } catch (ParseException e) {
            log.error("Token parsing failed: {}", e.getMessage());
            valid = false;
        } catch (AppException e) {
            log.error("Token validation failed: {}", e.getMessage());
            valid = false;
        } catch (Exception e) {
            log.error("Unexpected error during token introspection", e);
            valid = false;
        }

        return IntrospecResponse.builder()
                .valid(valid)
                .build();
    }

    // ================= VERIFY TOKEN =================
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Xác định thời gian hết hạn
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // Verify signature
        boolean verified = signedJWT.verify(verifier);

        // Kiểm tra token có hợp lệ và chưa hết hạn
        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
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

//     ================= REFRESH TOKEN =================
@Override
public LoginResponse refreshToken(RefreshTokenRequest request) {
    String refreshTokenValue = request.getRefreshToken();
    Long userId = request.getUserId();
    // Bước 1: Kiểm tra null hoặc empty
    if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty() || userId == null) {
        throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    // Bước 3: Tìm refresh token theo userId
    RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
            .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

    // Bước 4: Kiểm tra token trùng khớp (so sánh hash)
    if (!passwordEncoder.matches(refreshTokenValue, refreshToken.getTokenHash())) {
        throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    // Bước 5: Kiểm tra hết hạn
    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    // Bước 6: Lấy user
    User user = refreshToken.getUser();
    if (user == null || user.getStatus() != UserStatus.ACTIVE) {
        throw new AppException(ErrorCode.USER_NOT_FOUND);
    }

    // Bước 7: Sinh token mới
    String newAccessToken = generateAccessToken(user);
    String newRefreshToken = generateRefreshToken(); // random string mới

    // Bước 8: Cập nhật refresh token trong DB
    LocalDateTime expiresAt = LocalDateTime.ofInstant(
            Instant.now().plus(refreshTtlMinutes, ChronoUnit.MINUTES),
            ZoneId.systemDefault()
    );

    refreshToken.setTokenHash(passwordEncoder.encode(newRefreshToken));
    refreshToken.setExpiresAt(expiresAt);
    refreshTokenRepository.save(refreshToken);

    // Bước 9: Trả response
    LoginResponse res = mapper.toLoginResponse(user);
    res.setAccessToken(newAccessToken);
    res.setRefreshToken(newRefreshToken);
    return res;
}

    @Override
    public String currentId() {
        try {
            return ((Jwt) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal())
                    .getSubject();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
