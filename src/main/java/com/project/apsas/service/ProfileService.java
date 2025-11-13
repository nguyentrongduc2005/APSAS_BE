package com.project.apsas.service;

import com.project.apsas.dto.response.ProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;


public interface ProfileService {


    public ProfileResponse meFromJwt();
}
