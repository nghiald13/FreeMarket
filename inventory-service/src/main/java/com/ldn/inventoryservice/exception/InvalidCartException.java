package com.ldn.inventoryservice.exception;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCartException extends BusinessException {
    private final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public InvalidCartException(String message) {
        super(message);
    }
}
