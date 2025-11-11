package com.project.apsas.service;

import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.ResendCodeRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);          // giữ void
    void verify(VerifyRequest request);              // thêm verify
    LoginResponse login(LoginRequest request);
    void resendCode(ResendCodeRequest request);      // đúng tên theo controller
}
