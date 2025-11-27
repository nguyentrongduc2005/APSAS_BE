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
        String fieldName = "unknown";
        String errorMessage = "Validation failed";
        
        if (ex.getFieldError() != null) {
            fieldName = ex.getFieldError().getField();
            String defaultMessage = ex.getFieldError().getDefaultMessage();
            
            // Try to parse as ErrorCode, if fails use the message directly
            try {
                ErrorCode error = ErrorCode.valueOf(defaultMessage);
                errorMessage = error.getDefaultMessage();
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setCode(error.getCode());
                apiResponse.setMessage(errorMessage);
                return ResponseEntity.badRequest().body(apiResponse);
            } catch (IllegalArgumentException | NullPointerException e) {
                // If validation message is not an ErrorCode, use it as message
                errorMessage = defaultMessage != null ? defaultMessage : "Validation failed for field: " + fieldName;
            }
        }
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.VALIDATION_FAILED.getCode());
        apiResponse.setMessage(errorMessage);
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
