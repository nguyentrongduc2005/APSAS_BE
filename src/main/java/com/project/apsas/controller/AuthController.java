package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

     @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse res = authService.register(request);
        return ApiResponse.<RegisterResponse>builder()
                .code("OK")
                .message("Đăng ký thành công, vui lòng kiểm tra email để lấy mã xác thực")
                .data(res)
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verify(@Valid @RequestBody VerifyRequest request) {
        authService.verify(request);
        return ApiResponse.<Void>builder()
                .code("OK")
                .message("Xác thực thành công, tài khoản đã được kích hoạt")
                .build();
    }
}
