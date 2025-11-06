package com.project.apsas.exception;


import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("E4000", HttpStatus.BAD_REQUEST, "Request không hợp lệ."),
    VALIDATION_FAILED("E4001", HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ."),
    UNAUTHORIZED("E4010", HttpStatus.UNAUTHORIZED, "Chưa đăng nhập."),
    FORBIDDEN("E4030", HttpStatus.FORBIDDEN, "Không đủ quyền."),
    NOT_FOUND("E4040", HttpStatus.NOT_FOUND, "Không tìm thấy."),
    CONFLICT("E4090", HttpStatus.CONFLICT, "Xung đột dữ liệu."),
    DUPLICATE("E4091", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại."),
    UNSUPPORTED_MEDIA("E4150", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type không hỗ trợ."),
    INTERNAL_ERROR("E5000", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống.");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code; this.status = status; this.defaultMessage = defaultMessage;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
    public String defaultMessage() { return defaultMessage; }
}
