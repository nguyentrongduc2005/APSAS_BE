package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Properties;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    MailService mailService;

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest req) {
        // 1) Check trùng email
        if (userRepository.existsByEmail(req.getEmail())) {
            return ApiResponse.builder()
                    .code("EMAIL_EXISTS")
                    .message("Email đã tồn tại")
                    .build();
        }

        // 2) Map accountType -> roleName
        String roleName = switch (req.getAccountType()) {
            case 1 -> "USER";      // student
            case 2 -> "LECTURER";  // teacher
            default -> throw new IllegalArgumentException("accountType phải là 1 (student) hoặc 2 (teacher)");
        };

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role '" + roleName + "' chưa được seed"));

        // 3) Hash password & lưu user
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .status(UserStatus.ACTIVE) // hoặc INACTIVE nếu muốn xác minh email
                .build();
        user.getRoles().add(role);

        userRepository.save(user);

        // 4) Gửi mail (async)
        Properties params = new Properties();
        params.put("name", user.getName());
        params.put("email", user.getEmail());
        // tuỳ template Brevo của bạn: ví dụ {{name}}, {{email}}
        mailService.sendMailAsync(user.getEmail(), user.getName(), params);

        // 5) Trả về response
        return ApiResponse.builder()
                .code("OK")
                .message("Đăng ký thành công")
                .data(new Object() {
                    public final Long id = user.getId();
                    public final String email = user.getEmail();
                    public final String role = roleName;
                })
                .build();
    }
}
