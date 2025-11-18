package com.project.apsas.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
