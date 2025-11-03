package com.project.apsas.service.impl;

import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;
import com.project.apsas.mapper.UserMapper;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Incorrect password");
        }

        return userMapper.toLoginResponse(user);
    }
}
