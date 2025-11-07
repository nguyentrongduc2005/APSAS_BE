package com.project.apsas.exception;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String code;                
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data, String path) {
        return ApiResponse.<T>builder()
                .success(true).code("OK").message("Success")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message,
                                           String path) {
        return ApiResponse.<T>builder()
                .success(false).code(code).message(message)
                .timestamp(OffsetDateTime.now())
                .meta(meta)
                .build();
    }
}

