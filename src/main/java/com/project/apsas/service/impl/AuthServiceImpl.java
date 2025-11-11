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
import com.project.apsas.entity.User;
import com.project.apsas.entity.VerificationCode;
import com.project.apsas.enums.UserStatus;

import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.repository.VerificationCodeRepository;
import com.project.apsas.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    VerificationCodeRepository verificationCodeRepository;
    PasswordEncoder passwordEncoder;
    UserMapper mapper;
    KafkaMailProducer mailProducer;

    @Value("${security.jwt.secret}")
    String jwtSecret;

    @Value("${security.jwt.issuer:apsas}")
    String jwtIssuer;

    @Value("${security.jwt.access_ttl_minutes:60}")
    long accessTtlMinutes;

    static final String TOPIC_MAIL = "mail.send";
    static final int VERIFY_TTL_MINUTES = 10;

    // ================= REGISTER =================
    @Override
    public RegisterResponse register(RegisterRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(email).ifPresent(u -> {
            throw new RuntimeException("Email đã tồn tại");
        });

        String hashed = passwordEncoder.encode(req.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setName(req.getName());
        user.setStatus(UserStatus.INACTIVE); // ACTIVE sau khi verify

        userRepository.save(user);

        // OTP lưu ở VerificationCode
        String code = genOtp6();
        Instant expiresAt = Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES);

        verificationCodeRepository.deleteByEmail(email);

        VerificationCode vc = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(Instant.now())
                .build();
        verificationCodeRepository.save(vc);

        sendOtpMail(email, user.getName(), code, VERIFY_TTL_MINUTES);
        return null;
    }

    @Override
    public void verify(VerifyRequest request) {

    }

    // ================= LOGIN =================
    @Override
    public LoginResponse login(LoginRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản chưa xác thực email");
        }

        String accessToken = generateAccessToken(user);

        // Trả về theo mapper hiện có của bạn
        LoginResponse res = mapper.toLoginResponse(user);


        // Nếu LoginResponse của bạn có setter/constructor cho token thì set thêm ở đây.
        // Tuỳ tên field trong DTO mà chỉnh 'setAccessToken' hay 'setToken':
        try {
            res.getClass().getMethod("setAccessToken", String.class).invoke(res, accessToken);
        } catch (Exception ignored1) {
            try {
                res.getClass().getMethod("setToken", String.class).invoke(res, accessToken);
            } catch (Exception ignored2) {
                // Nếu DTO là immutable/record, hãy bổ sung field token trong mapper/DTO sau.
            }
        }
        return res;
    }

    // ================= RESEND OTP =================
    @Override
    public void resendCode(ResendCodeRequest req) {
        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String code = genOtp6();
        Instant expiresAt = Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES);

        verificationCodeRepository.deleteByEmail(email);

        VerificationCode vc = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(Instant.now())
                .build();
        verificationCodeRepository.save(vc);

        sendOtpMail(email, user.getName(), code, VERIFY_TTL_MINUTES);
    }

    // ================= Helpers =================
    private String genOtp6() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void sendOtpMail(String to, String name, String otp, int minutesLeft) {
        Properties params = new Properties();
        params.setProperty("otp", otp);
        params.setProperty("minutes", String.valueOf(minutesLeft));
        // Nếu template bên mail-service cần thêm, bạn có thể add tiếp vào params:
        // params.setProperty("template", "otp");

        SendMailEvent event = SendMailEvent.builder()
                .toEmail(to)
                .name(name)
                .params(params)
                .build();


        mailProducer.push(TOPIC_MAIL, "OTP-" + to, event);
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
            throw new RuntimeException("Không tạo được access token");
        }
    }
}
