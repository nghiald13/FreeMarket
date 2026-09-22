package com.ldn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final int statusCode;

    // 1. Mặc định nếu chỉ truyền message thì statusCode = 400 BAD_REQUEST
    public BusinessException(String message) {
        super(message);
        this.statusCode = HttpStatus.BAD_REQUEST.value();
    }

    // 2. Truyền custom statusCode kiểu int (VD: 404, 409, 422)
    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    // 3. Truyền custom statusCode kiểu HttpStatus enum của Spring
    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.statusCode = httpStatus.value();
    }
}
