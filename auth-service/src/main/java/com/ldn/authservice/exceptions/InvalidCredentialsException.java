package com.ldn.authservice.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {
    private static final String message = "Invalid credentials!";

    public InvalidCredentialsException() {
        super(message);
    }
}
