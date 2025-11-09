package com.project.apsas.service;

import com.nimbusds.jose.JOSEException;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.request.RegisterRequest;
import com.project.apsas.dto.request.VerifyRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.dto.response.RegisterResponse;

import java.text.ParseException;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    void verify(VerifyRequest request);
    LoginResponse login(LoginRequest loginRequest);
    IntrospecResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;
}
