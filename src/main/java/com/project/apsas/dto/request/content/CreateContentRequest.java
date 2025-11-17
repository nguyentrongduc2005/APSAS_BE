package com.project.apsas.dto.request.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateContentRequest {
    private String title;


    // @NotEmpty
    private String bodyMd; // Client sẽ gửi markdown ở đây

    private Integer orderNo;
}
