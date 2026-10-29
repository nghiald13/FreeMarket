package com.ldn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final int statusCode;
    private final String error;

    // 1. Mặc định nếu chỉ truyền message thì statusCode = 400 BAD_REQUEST
    public BusinessException(String message, String error) {
        super(message);
        this.error = error;
        this.statusCode = HttpStatus.BAD_REQUEST.value();
    }

    // 2. Truyền custom statusCode kiểu int (VD: 404, 409, 422)
    public BusinessException(String message, int statusCode, String error) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
    }

    // 3. Truyền custom statusCode kiểu HttpStatus enum của Spring
    public BusinessException(String message, HttpStatus httpStatus, String error) {
        super(message);
        this.statusCode = httpStatus.value();
        this.error = error;
    }
}
