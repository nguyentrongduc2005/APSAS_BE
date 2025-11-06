package com.project.apsas.exception;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode; this.args = null;
    }
    public AppException(ErrorCode errorCode, String message, Object... args) {
        super(message); this.errorCode = errorCode; this.args = args;
    }
    public ErrorCode getErrorCode() { return errorCode; }
    public Object[] getArgs() { return args; }
}
