package com.project.apsas.exception;

public class NotFoundException extends AppException {
    public NotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, resource + " id=" + id + " không tồn tại.");
    }
}

