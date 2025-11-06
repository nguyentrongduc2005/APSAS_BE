package com.project.apsas.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Tự bọc mọi response từ @RestController vào ApiResponse (success). */
@Component
public class ResponseEnvelopeAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ResponseEnvelopeAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getContainingClass().isAnnotationPresent(RestController.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ApiResponse<?> api) return api;

        String path = request.getURI().getPath();
        String traceId = MDC.get("traceId"); // đọc trực tiếp từ MDC

        if (body instanceof String s) {
            try {
                return objectMapper.writeValueAsString(ApiResponse.ok(s, path, traceId));
            } catch (Exception e) {
                return s;
            }
        }
        return ApiResponse.ok(body, path, traceId);
    }
}
