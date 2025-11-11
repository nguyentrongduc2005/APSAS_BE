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

    // 1 = student, 2 = teacher (nếu bạn chưa dùng thì vẫn để đây cho đúng yêu cầu)
    private Integer role;

    // optional
    private String avatar;
}
