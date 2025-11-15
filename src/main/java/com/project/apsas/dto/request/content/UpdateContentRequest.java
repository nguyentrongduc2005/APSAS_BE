package com.project.apsas.dto.request.content;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateContentRequest {
    private String title;


    // @NotEmpty
    private String bodyMd; // Client sẽ gửi markdown ở đây

    private Integer orderNo;

    private List<Long> filesDelete;
}
