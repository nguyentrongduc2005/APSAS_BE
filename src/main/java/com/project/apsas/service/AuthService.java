package com.project.apsas.service;

import com.nimbusds.jose.JOSEException;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;

import java.text.ParseException;

public interface AuthService {
    public LoginResponse login(LoginRequest loginRequest);
    public IntrospecResponse introspect(IntrospectRequest introspectRequest)   throws JOSEException, ParseException;
}
