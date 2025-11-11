package com.project.apsas.service;

import com.project.apsas.dto.response.ProfileResponse;
import com.project.apsas.entity.User;
import com.project.apsas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;


public interface ProfileService {


    public ProfileResponse meFromJwt(Jwt jwt);
}
