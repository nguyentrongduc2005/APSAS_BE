package com.project.apsas.service;

import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;

public interface AuthService {
    public LoginResponse login(LoginRequest loginRequest);

}
