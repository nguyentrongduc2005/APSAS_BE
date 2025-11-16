package com.project.apsas.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileResponse {
    Long id;
    String name;
    String email;
    LocalDate dob;
    String bio;
    String phone;
    String address;
    String gender;
    String avatar;
    boolean success;
}
