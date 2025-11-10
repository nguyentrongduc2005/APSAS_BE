package com.project.apsas.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;   // JWT
    private String refreshToken;  // lưu DB
    private AuthUserDto user;     // id, name, email, roles, avatar
}
