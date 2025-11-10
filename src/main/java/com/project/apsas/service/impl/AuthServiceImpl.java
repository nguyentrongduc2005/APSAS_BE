package com.project.apsas.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.entity.*;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.integration.kafka.mail.KafkaMailProducer;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.*;
import com.project.apsas.service.AuthService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    private static final int REFRESH_TTI_DAYS = 7;
    UserRepository userRepository;
    RoleRepository roleRepository;
    VerificationCodeRepository verificationCodeRepository;
    RefreshTokenRepository refreshTokenRepository;

    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    KafkaMailProducer kafkaMailProducer; 

    @NonFinal @Value("${jwt.signerKey}") String SIGNER_KEY;

    @NonFinal @Value("${app.verify.ttl-minutes:10}") long VERIFY_TTL_MINUTES;
    @NonFinal @Value("${app.kafka.topic.mail-send:mail-send}") String MAIL_TOPIC;
    @NonFinal @Value("${app.mail.template.verify:VERIFY_EMAIL}") String VERIFY_TEMPLATE;

    @NonFinal @Value("${app.auth.access-ttl-minutes:15}") long ACCESS_TTL_MINUTES;   // 15'
    @NonFinal @Value("${app.auth.refresh-ttl-days:7}") long REFRESH_TTL_DAYS;       // 7 days

    /* ================= Helpers ================= */

    private String genCode() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }

    private String generateAccessToken(User user, long ttlMinutes) {
        try {
            Instant now = Instant.now();

            Set<String> roleNames = new HashSet<>();
            user.getRoles().forEach(r -> roleNames.add(r.getName()));

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.getId()))
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("roles", roleNames)
                    .build();

            JWSHeader header = new JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS512);
            JWSObject jws = new JWSObject(header, new Payload(claims.toJSONObject()));
            jws.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jws.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Không tạo được access token", e);
        }
    }

    private String newRefreshToken() {
        // Có thể dùng SecureRandom/UUID, ở đây đủ cho use-case
        return UUID.randomUUID().toString().replace("-", "");
    }

    /* ================= Register: INACTIVE + OTP lưu DB + gửi Kafka ================= */

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String roleName = switch (request.getAccountType()) {
            case 1 -> "USER";
            case 2 -> "LECTURER";
            default -> throw new RuntimeException("accountType phải là 1 (student) hoặc 2 (teacher)");
        };

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.INACTIVE)
                .roles(new HashSet<>(Collections.singleton(role)))
                .build();
        userRepository.save(user);

        // clear mã cũ (nếu có) và lưu mã mới
        verificationCodeRepository.deleteByEmail(email);
        String code = genCode();
        VerificationCode vc = VerificationCode.builder()
                .email(email)
                .code(code)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(VERIFY_TTL_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .build();
        verificationCodeRepository.save(vc);

        // gửi qua Kafka -> mail-service (Brevo)
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

    /* ================= Verify: đối chiếu DB, ACTIVE, mark used ================= */

    @Override
    public void verify(VerifyRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        VerificationCode latest = verificationCodeRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("Không có mã xác thực"));

        if (latest.isUsed() || Instant.now().isAfter(latest.getExpiresAt())) {
            throw new RuntimeException("Mã xác thực đã dùng hoặc hết hạn");
        }
        if (!latest.getCode().equals(request.getCode())) {
            throw new RuntimeException("Mã xác thực không đúng");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        latest.setUsed(true);
        verificationCodeRepository.save(latest);
    }

    /* ================= Login: tạo access + refresh, lưu refresh DB ================= */

    @Override
    public LoginResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Sai email hoặc mật khẩu"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản chưa kích hoạt. Vui lòng xác thực email.");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai email hoặc mật khẩu");
        }

        String accessToken = generateAccessToken(user, ACCESS_TTL_MINUTES); 

        Instant expiryInstant = Instant.now().plus(REFRESH_TTI_DAYS, ChronoUnit.DAYS);

        // 2. Chuyển đổi Instant sang LocalDateTime để lưu vào DB (dùng múi giờ hệ thống)
        LocalDateTime expiryDateTime = LocalDateTime.ofInstant(expiryInstant, ZoneId.systemDefault());

        // refresh token mới -> (tuỳ yêu cầu) xóa refresh cũ của user rồi tạo mới
        refreshTokenRepository.deleteByUser(user);
        String refreshTokenStr = newRefreshToken();
        
       RefreshToken rt = RefreshToken.builder()
            .userId(user.getId()) 
            .tokenHash(refreshTokenStr) 
            .expiresAt(expiryDateTime)
            .build();

        refreshTokenRepository.save(rt);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(userMapper.toAuthUserDto(user))
                .build();
    }

    /* ================= Introspect: verify chữ ký + hạn ================= */

    @Override
    public IntrospecResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        SignedJWT signed = SignedJWT.parse(token);
        boolean verified = signed.verify(new MACVerifier(SIGNER_KEY.getBytes()));

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
