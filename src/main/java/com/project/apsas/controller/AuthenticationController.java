package com.project.apsas.controller;

import com.nimbusds.jose.JOSEException;
import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.IntrospectRequest;
import com.project.apsas.dto.response.IntrospecResponse;
import com.project.apsas.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthService authService;

//    @PostMapping("/introspect")
//    ApiResponse<IntrospecResponse> introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
//        var result = authService.introspect(introspectRequest );
//        return ApiResponse.<IntrospecResponse>builder()
//                .data(result)
//                .build();
//    }

}
