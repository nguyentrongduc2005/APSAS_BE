package com.project.apsas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthUserDto {
    private Long id;
    private String name;
    private String email;
    private Set<String> roles;
    private String avatar; 
}
