package com.project.apsas.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.MailService; // vẫn giữ nếu nơi khác đang inject; không dùng trong OTP

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    MailService mailService;          // không dùng trong OTP nhưng giữ lại để tránh phá chỗ khác
    UserMapper userMapper;

    StringRedisTemplate redis;
    KafkaMailProducer kafkaMailProducer;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${app.verify.ttl-minutes:10}")
    Long VERIFY_TTL_MINUTES;

    @NonFinal
    @Value("${app.kafka.topic.mail-send:mail-send}")
    String MAIL_TOPIC;

    @NonFinal
    @Value("${app.mail.template.verify:VERIFY_EMAIL}")
    String VERIFY_TEMPLATE;

    /* ===================== Helpers ===================== */

    private String verifyKey(String email) {
        return "verify:" + email.toLowerCase();
    }

    private String genCode() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }

    private String generateToken(User user) {
        try {
            Instant now = Instant.now();

            Set<String> roleNames = new HashSet<>();
            user.getRoles().forEach(r -> roleNames.add(r.getName()));

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.getId()))
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(1, ChronoUnit.DAYS))) // TTL 1 ngày (tùy chỉnh thêm nếu muốn)
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("roles", roleNames)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Không tạo được token", e);
        }
    }

    /* ===================== REGISTER (INACTIVE + OTP + Kafka) ===================== */

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String roleName = switch (request.getAccountType()) {
            case 1 -> "USER";      // Student
            case 2 -> "LECTURER";  // Teacher
            default -> throw new RuntimeException("accountType phải là 1 (student) hoặc 2 (teacher)");
        };

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        // builder roles: đảm bảo không NPE khi add
        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // HASH BCrypt
                .status(UserStatus.INACTIVE)                            // đăng ký xong INACTIVE
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        userRepository.save(user);

        // Tạo OTP và lưu Redis với TTL
        String code = genCode();
        redis.opsForValue().set(verifyKey(email), code, Duration.ofMinutes(VERIFY_TTL_MINUTES));

        // Gửi OTP qua Kafka (mail-service sẽ gửi email)
        Properties params = new Properties();
        params.setProperty("template", VERIFY_TEMPLATE);
        params.setProperty("name", user.getName());
        params.setProperty("email", email);
        params.setProperty("code", code);
        params.setProperty("ttlMinutes", String.valueOf(VERIFY_TTL_MINUTES));

        SendMailEvent event = SendMailEvent.builder()
                .toEmail(email)
                .name(user.getName())
                .params(params)
                .build();

        kafkaMailProducer.push(MAIL_TOPIC, email, event);

        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }

    /* ===================== VERIFY OTP ===================== */

    @Override
    public void verify(VerifyRequest verifyRequest) {
        String email = verifyRequest.getEmail().trim().toLowerCase();
        String key = verifyKey(email);
        String codeInRedis = redis.opsForValue().get(key);

        if (codeInRedis == null) {
            throw new RuntimeException("Mã xác thực không tồn tại hoặc đã hết hạn");
        }
        if (!codeInRedis.equals(verifyRequest.getCode())) {
            throw new RuntimeException("Mã xác thực không đúng");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        redis.delete(key);
    }

    /* ===================== LOGIN (JWT) ===================== */

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Sai email hoặc mật khẩu"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản chưa kích hoạt. Vui lòng xác thực email trước khi đăng nhập.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai email hoặc mật khẩu");
        }

        String token = generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toAuthUserDto(user)) // trả name,email,roles,avatar
                .build();
    }

    /* ===================== INTROSPECT ===================== */

    @Override
    public IntrospecResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        String token = introspectRequest.getToken();
    SignedJWT signed = SignedJWT.parse(token);
    boolean verified = signed.verify(new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));

        boolean active = false;
        if (verified) {
            Date now = new Date();
            Date exp = signed.getJWTClaimsSet().getExpirationTime();
            active = (exp != null && exp.after(now));
        }

    return IntrospecResponse.builder()
        .valid(active)
        .build();
    }
}
