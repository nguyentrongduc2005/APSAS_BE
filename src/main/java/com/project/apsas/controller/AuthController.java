package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.ResendCodeRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
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

     @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse res = authService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .code("OK")
                .message("Đăng nhập thành công")
                .data(res)
                .build();
    }

    @PostMapping("/resend-code")
    public ApiResponse<Void> resend(@Valid @RequestBody ResendCodeRequest request) {
        authService.resendCode(request);
        return ApiResponse.<Void>builder()
                .code("OK")
                .message("Đã gửi lại mã xác thực tới email")
                .build();
    }
}
