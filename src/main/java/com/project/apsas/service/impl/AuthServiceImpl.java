package com.project.apsas.service.impl;

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
import com.project.apsas.service.MailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Properties;

 

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    MailService mailService;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

     StringRedisTemplate redis;
    KafkaMailProducer kafkaMailProducer; 

    @Value("${app.verify.ttl-minutes:10}")
    @NonFinal Long VERIFY_TTL_MINUTES;

    @Value("${app.kafka.topic.mail-send:mail-send}")
    @NonFinal String MAIL_TOPIC;

    @Value("${app.mail.template.verify:VERIFY_EMAIL}")
    @NonFinal String VERIFY_TEMPLATE;

    private String verifyKey(String email) {
        return "verify:" + email.toLowerCase();
    }
    private String genCode() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }

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

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // HASH bằng BCrypt
                .status(UserStatus.ACTIVE)
                .build();
        user.getRoles().add(role);

        userRepository.save(user);

        // Gửi mail async (Brevo/Kafka flow của bạn bên dưới MailServiceImpl)
        Properties params = new Properties();
        params.setProperty("name", user.getName());
        params.setProperty("app_brand", "APSAS");
        try {
            mailService.sendMailAsync(user.getEmail(), user.getName(), params);
        } catch (Exception ignored) {}

        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Minimal stub to satisfy interface; implement real logic (JWT creation, password check) later
        throw new UnsupportedOperationException("login not implemented yet");
    }

    @Override
    public IntrospecResponse introspect(IntrospectRequest introspectRequest) throws com.nimbusds.jose.JOSEException, java.text.ParseException {
        // Minimal stub to satisfy interface; implement token introspection logic later
        throw new UnsupportedOperationException("introspect not implemented yet");
    }

    @Override
    public void verify(VerifyRequest verifyRequest) {
        // Implement verification logic here
        throw new UnsupportedOperationException("verify not implemented yet");
    }
}
