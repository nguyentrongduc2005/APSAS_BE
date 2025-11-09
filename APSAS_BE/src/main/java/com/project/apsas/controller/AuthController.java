package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // URL đầy đủ: /api/auth/...
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest req) {
        var res = authService.register(req);
        return ResponseEntity.ok(ApiResponse.<RegisterResponse>builder()
                .code("OK")
                .message("Tạo tài khoản thành công, vui lòng kiểm tra email để xác minh")
                .data(res)
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyRequest req) {
        authService.verify(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code("OK")
                .message("Xác minh thành công, tài khoản đã được kích hoạt")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        var res = authService.login(req);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .code("OK")
                .message("Đăng nhập thành công")
                .data(res)
                .build());
    }
}
