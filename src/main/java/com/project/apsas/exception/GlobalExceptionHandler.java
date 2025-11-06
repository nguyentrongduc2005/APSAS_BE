package com.project.apsas.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<Object>> resp(ErrorCode ec, String msg,
                                                     HttpServletRequest req, Map<String, Object> meta) {
        var body = ApiResponse.error(
                ec.code(),
                (msg != null && !msg.isBlank()) ? msg : ec.defaultMessage(),
                req.getRequestURI(),
                MDC.get("traceId"),      // không phụ thuộc tên filter
                meta
        );
        return ResponseEntity.status(ec.status()).body(body);
    }

    @ExceptionHandler(AppException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleApp(AppException ex, HttpServletRequest req) {
        return resp(ex.getErrorCode(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleInvalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return resp(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleInvalidParams(ConstraintViolationException ex, HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", Optional.ofNullable(cv.getPropertyPath()).map(Object::toString).orElse(""),
                        "message", Optional.ofNullable(cv.getMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return resp(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, Map.of("errors", errors));
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleBind(BindException ex, HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return resp(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, Map.of("errors", errors));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class })
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(Exception ex, HttpServletRequest req) {
        return resp(ErrorCode.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handle404(NoHandlerFoundException ex, HttpServletRequest req) {
        return resp(ErrorCode.NOT_FOUND, "Endpoint không tồn tại.", req, null);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return resp(ErrorCode.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return resp(ErrorCode.CONFLICT, msg, req, null);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleAll(Throwable ex, HttpServletRequest req) {
        return resp(ErrorCode.INTERNAL_ERROR, ex.getMessage(), req, null);
    }
}
