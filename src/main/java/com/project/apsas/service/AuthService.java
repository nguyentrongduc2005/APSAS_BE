package com.project.apsas.service;

import com.project.apsas.dto.request.*;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);          // giữ void
    void verify(VerifyRequest request);              // thêm verify
    LoginResponse login(LoginRequest request);
    void resendCode(ResendCodeRequest request);
    IntrospecResponse introspect(IntrospectRequest request);// đúng tên theo controller
    String currentId();
    LoginResponse refreshToken(RefreshTokenRequest request);
    // đúng tên theo controller
}
