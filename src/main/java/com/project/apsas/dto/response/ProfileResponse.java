package com.project.apsas.dto.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String avatar; // nếu có
    private List<String> roles;
}
