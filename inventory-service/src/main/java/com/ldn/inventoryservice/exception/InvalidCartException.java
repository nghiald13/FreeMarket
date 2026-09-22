package com.ldn.inventoryservice.exception;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCartException extends BusinessException {

    public InvalidCartException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
