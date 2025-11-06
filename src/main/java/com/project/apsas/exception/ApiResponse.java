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
    private String path;
    private String traceId;
    private OffsetDateTime timestamp;
    private Map<String, Object> meta;    

    public static <T> ApiResponse<T> ok(T data, String path, String traceId) {
        return ApiResponse.<T>builder()
                .success(true).code("OK").message("Success")
                .data(data).path(path).traceId(traceId)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message,
                                           String path, String traceId,
                                           Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .success(false).code(code).message(message)
                .path(path).traceId(traceId)
                .timestamp(OffsetDateTime.now())
                .meta(meta)
                .build();
    }
}

