package com.project.apsas.controller;

import com.nimbusds.jose.JOSEException;
import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.*;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;
import com.project.apsas.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

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
    @PostMapping("/introspect")
    public ApiResponse<IntrospecResponse> introspect(@Valid @RequestBody IntrospectRequest request){
        IntrospecResponse response = authService.introspect(request);
        return ApiResponse.<IntrospecResponse>builder()
                .code("OK")
                .message("Token hợp lệ")
                .data(response)
                .build();
    }
    @PostMapping("/refresh-token")
        public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        LoginResponse response = authService.refreshToken(request);
        return ApiResponse.<LoginResponse>builder()
                .code("OK")
                .message("Đã Cấp nhật Token Mới")
                .data(response)
                .build();
    }

}
