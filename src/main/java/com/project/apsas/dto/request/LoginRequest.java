package com.project.apsas.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}
