package com.project.apsas.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    private String name;

    @NotNull(message = "Role không được để trống")
    @Min(value = 1, message = "Role phải là 1 (student) hoặc 2 (teacher)")
    @Max(value = 2, message = "Role phải là 1 (student) hoặc 2 (teacher)")
    private Integer role;

    // optional
    private String avatar;
}
