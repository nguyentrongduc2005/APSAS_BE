package com.project.apsas.dto.request.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CreateContentRequest {
    private String title;


    // @NotEmpty
    private String bodyMd; // Client sẽ gửi markdown ở đây

    private Integer orderNo;
}
