package com.project.apsas.exception;

import com.project.apsas.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ex.getErrorCode().getCode());
        apiResponse.setMessage(ex.getErrorCode().getDefaultMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleMethodAccessDeniedException(AuthorizationDeniedException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.ACCESS_DENIED.getCode());
        apiResponse.setMessage(ErrorCode.ACCESS_DENIED.getDefaultMessage());

        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorCode = ex.getFieldError().getDefaultMessage();
        ErrorCode error = ErrorCode.valueOf(errorCode);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(error.getCode());
        apiResponse.setMessage(error.getDefaultMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }


    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(Exception ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.INTERNAL_ERROR.getCode());
        apiResponse.setMessage(ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        ex.printStackTrace();

        return ResponseEntity.internalServerError().body(apiResponse);
    }

}
