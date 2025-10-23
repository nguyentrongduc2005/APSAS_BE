package com.project.apsas.controller;

import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class demo {
    @Autowired
    UserRepository userRepository;
    @GetMapping("/hello")
    public String hello() {
        System.out.println(UserStatus.ACTIVE.name());
        User user = User.builder()
                .email("sdfsdfsdfsdfsdf")
                .password("sdfsdfsdfsdfsdfsdf")
                .name("sdfsdfsdfsdfsdfsdfsdf")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        return "Hello World";
    }
}
