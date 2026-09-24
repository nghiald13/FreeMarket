package com.ldn.common.advice;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        int statusCode = HttpStatus.BAD_REQUEST.value();

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error("Validation failed", fieldErrors, statusCode));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound404(NoHandlerFoundException ex) {
        int statusCode = HttpStatus.NOT_FOUND.value();
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error("Endpoint not found: " + ex.getRequestURL(), null, statusCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error("Internal server error", ex.getMessage(), statusCode));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        int statusCode = ex.getStatusCode();

        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error(ex.getMessage(), null, statusCode));
    }
}
