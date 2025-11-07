package com.project.apsas.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {


    private ProblemDetail pd(ErrorCode ec, String message, HttpServletRequest req) {
        HttpStatus status = ec.status();
        ProblemDetail p = ProblemDetail.forStatusAndDetail(
                status,
                (message != null && !message.isBlank()) ? message : ec.defaultMessage()
        );
        p.setTitle(status.getReasonPhrase());
        p.setType(URI.create("about:blank"));
        p.setProperty("code", ec.code());
        p.setProperty("timestamp", OffsetDateTime.now().toString());
        p.setProperty("path", req.getRequestURI());
        p.setProperty("method", req.getMethod());
        p.setProperty("traceId", MDC.get("traceId"));
        return p;
    }
    private ProblemDetail pdWithErrors(ErrorCode ec, String msg, HttpServletRequest req,
                                       List<Map<String,String>> errors) {
        ProblemDetail p = pd(ec, msg, req);
        p.setProperty("errors", errors);
        return p;
    }

   
    @ExceptionHandler(AppException.class)
    public ProblemDetail handleApp(AppException ex, HttpServletRequest req) {
        return pd(ex.getErrorCode(), ex.getMessage(), req);
    }

 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String,String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return pdWithErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errors);
    }

   
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleInvalidParams(ConstraintViolationException ex, HttpServletRequest req) {
        List<Map<String,String>> errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", Optional.ofNullable(cv.getPropertyPath()).map(Object::toString).orElse(""),
                        "message", Optional.ofNullable(cv.getMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return pdWithErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errors);
    }

 
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBind(BindException ex, HttpServletRequest req) {
        List<Map<String,String>> errors = ex.getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid")))
                .collect(Collectors.toList());
        return pdWithErrors(ErrorCode.VALIDATION_FAILED, "Dữ liệu không hợp lệ.", req, errors);
    }


    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest req) {
        return pd(ErrorCode.BAD_REQUEST, ex.getMessage(), req);
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handle404(NoHandlerFoundException ex, HttpServletRequest req) {
        return pd(ErrorCode.NOT_FOUND, "Endpoint không tồn tại.", req);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return pd(ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return pd(ErrorCode.CONFLICT, msg, req);
    }


    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleAll(Throwable ex, HttpServletRequest req) {
        return pd(ErrorCode.BAD_REQUEST, ex.getMessage(), req);
    }
}
