package com.project.apsas.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.project.apsas.exception.RequestCorrelationFilter.MDC_KEY;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail base(ErrorCode ec, String message, HttpServletRequest req) {
        HttpStatus status = ec.status();
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, message != null ? message : ec.defaultMessage());
        p.setTitle(status.getReasonPhrase());
        p.setType(URI.create("about:blank"));
        p.setProperty("code", ec.code());
        p.setProperty("timestamp", OffsetDateTime.now().toString());
        p.setProperty("path", req.getRequestURI());
        p.setProperty("method", req.getMethod());
        p.setProperty("traceId", MDC.get(MDC_KEY));
        return p;
    }

    private ProblemDetail withErrors(ErrorCode ec, String message, HttpServletRequest req, List<Map<String,String>> errors) {
        ProblemDetail p = base(ec, message, req);
        p.setProperty("errors", errors);
        return p;
    }

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleApp(AppException ex, HttpServletRequest req) {
        log.warn("AppException: {}", ex.getMessage());
        return base(ex.getErrorCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var errs = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .toList();
        return withErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errs);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleInvalidParams(ConstraintViolationException ex, HttpServletRequest req) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        List<Map<String, String>> errs = violations.stream()
            .map(cv -> Map.of(
                "field", extractFieldName(cv.getPropertyPath().toString()),
                "message", Optional.ofNullable(cv.getMessage()).orElse("invalid")
            ))
            .toList();

        return withErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errs);
    }

    /**
     * Extract field name from property path (e.g., "getUser.id" -> "id")
     */
    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "";
        }
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBind(BindException ex, HttpServletRequest req) {
        var errs = ex.getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .toList();
        return withErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errs);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class })
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest req) {
        return base(ErrorCode.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return base(ErrorCode.BAD_REQUEST, "Method không hỗ trợ.", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return base(ErrorCode.UNSUPPORTED_MEDIA, ex.getMessage(), req);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handle404(NoHandlerFoundException ex, HttpServletRequest req) {
        return base(ErrorCode.NOT_FOUND, "Endpoint không tồn tại.", req);
    }

    // Nếu dự án có Spring Security, có thể thêm 2 handler cho Authentication/AccessDenied.
    // Nếu CHƯA dùng Security, bỏ qua để tránh import lỗi.

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return base(ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return base(ErrorCode.CONFLICT, msg, req);
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleAll(Throwable ex, HttpServletRequest req) {
        log.error("Unhandled error", ex);
        return base(ErrorCode.INTERNAL_ERROR, ex.getMessage(), req);
    }
}