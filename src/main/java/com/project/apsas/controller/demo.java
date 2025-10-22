package com.project.apsas.controller;

import com.project.apsas.enums.UserStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class demo {
    @GetMapping("/hello")
    public String hello() {
        System.out.println(UserStatus.ACTIVE.name());
        return "Hello World";
    }
}
