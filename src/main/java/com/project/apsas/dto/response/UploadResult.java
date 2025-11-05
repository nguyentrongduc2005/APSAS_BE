package com.project.apsas.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    String publicId;
    String url;
    String resourceType;
    String format;
    Integer width;
    Integer height;
}
