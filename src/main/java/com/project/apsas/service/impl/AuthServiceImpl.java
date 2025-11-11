package com.project.apsas.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.ResendCodeRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.LoginResponse;

import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.entity.Otp;
import com.project.apsas.entity.RefreshToken;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;

import com.project.apsas.enums.UserStatus;

import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.OtpRepository;
import com.project.apsas.repository.RefreshTokenRepository;
import com.project.apsas.repository.UserRepository;

import com.project.apsas.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    UserRepository userRepository;
    OtpRepository otpRepository;
    PasswordEncoder passwordEncoder;
    UserMapper mapper;
    KafkaMailProducer mailProducer;
    RefreshTokenRepository  refreshTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    String jwtSecret;
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

        String hashed = passwordEncoder.encode(req.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setName(req.getName());
        user.setStatus(UserStatus.INACTIVE); // ACTIVE sau khi verify


        // OTP lưu ở VerificationCode
        String code = genOtp6();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES),
                ZoneId.systemDefault()) ;

        Otp vc = Otp.builder()
                .code(code)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
        user.setOtp(vc);
        userRepository.save(user);

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
        String userCode = user.getOtp().getCode();
        if(!(user.getStatus().equals(UserStatus.INACTIVE)
                && request.getCode().equals(userCode))) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
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
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
                    claims
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Generate access token error", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
