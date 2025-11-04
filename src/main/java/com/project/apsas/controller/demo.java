package com.project.apsas.controller;

import com.project.apsas.dto.request.LoginRequest;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.Permission;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.repository.PermissionRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;

@RestController
public class demo {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;
    @Autowired
    AuthService authService;
    @GetMapping("/hello")
    public String hello() {
        System.out.println(UserStatus.ACTIVE.name());
        Permission permission = Permission.builder()
                .name("update12")
                .description("update")
                .build();

        Role role = Role.builder()
                .name("admin12")
                .description("admin")
                .permissions(new HashSet<>() {{add(permission);}})
                .build();

        User user = User.builder()
                .email("duc@gmail.com")
                .password("123")
                .name("sdfsdfsdfsdfsdfsdfsdf")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>() {{add(role);}})
                .build();
        userRepository.save(user);
        return "Hello World";
    }




    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }



}
