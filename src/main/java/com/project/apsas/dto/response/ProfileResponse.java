package com.project.apsas.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String name;
    private String gender;
    private LocalDate dob;
    private String email;
    private String avatar; // nếu có
    private String address;
    private String phone;
    private String bio;
}
