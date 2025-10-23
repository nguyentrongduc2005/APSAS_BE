package com.project.apsas.controller;

import com.project.apsas.entity.Permission;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.repository.PermissionRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    @GetMapping("/hello")
    public String hello() {
        System.out.println(UserStatus.ACTIVE.name());
        Permission permission = Permission.builder()
                .name("read")
                .description("read")
                .build();

        Role role = Role.builder()
                .name("user")
                .description("user")
                .permissions(new HashSet<>() {{add(permission);}})
                .build();

        User user = User.builder()
                .email("sdfsdfsdfsdfsdf")
                .password("sdfsdfsdfsdfsdfsdf")
                .name("sdfsdfsdfsdfsdfsdfsdf")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>() {{add(role);}})
                .build();
        userRepository.save(user);
        return "Hello World";
    }
}
